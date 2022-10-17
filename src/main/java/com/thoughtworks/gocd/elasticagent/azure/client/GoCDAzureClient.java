/*
 * Copyright 2020 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.gocd.elasticagent.azure.client;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithCreate;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithNetwork;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithOS;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ProvisionFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.ImageURN;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import com.thoughtworks.gocd.elasticagent.azure.vm.AzureVMExtension;
import com.thoughtworks.gocd.elasticagent.azure.vm.PlatformConfigStrategy;
import com.thoughtworks.gocd.elasticagent.azure.vm.VmConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class GoCDAzureClient {

  private static final String FAILED_PROVISIONING_STATE = "failed";
  private NetworkDecorator networkDecorator;
  private Azure azure;
  private String resourceGroup;

  GoCDAzureClient(Azure azure, String resourceGroup, NetworkDecorator networkDecorator) {
    this.azure = azure;
    this.resourceGroup = resourceGroup;
    this.networkDecorator = networkDecorator;
  }

  public VirtualMachine createVM(VmConfig config) throws ProvisionFailedException {
    try {
      return buildVM(config);
    } catch (Exception e) {
      LOG.error("Failed to create vm with name {} for job {} due to error: {}\n{} {}", config.getName(), config.getJobIdentifier().getRepresentation(), e.getMessage(), e.toString());
      tearDown(config);
      throw new ProvisionFailedException(config.getJobIdentifier(), String.format("Failed to create vm with name %s for job %s due to error: %s", config.getName(), config.getJobIdentifier().getRepresentation(), e.getMessage()));
    }
  }

  private void tearDown(VmConfig config) {
    LOG.info("Tearing down vm {}", config.getName());
    VirtualMachine vm = azure.virtualMachines().getByResourceGroup(resourceGroup, config.getName());
    terminate(vm);
    cleanup(config);
  }

  private void cleanup(VmConfig config) {
    NetworkInterface nic = azure.networkInterfaces().getByResourceGroup(resourceGroup, config.getNetworkInterfaceName());
    if (nic != null) {
      LOG.info("Cleaning up Network Interface {} of the VM {}", config.getNetworkInterfaceName(), config.getName());
      azure.networkInterfaces().deleteById(nic.id());
    }
  }

  public VirtualMachine addTag(String vmId, String tagName, String tagValue) {
    VirtualMachine vm = azure.virtualMachines().getById(vmId);
    return vm.update().withTag(tagName, tagValue).apply();
  }

  public VirtualMachine removeTag(String vmId, String tagName) {
    VirtualMachine vm = azure.virtualMachines().getById(vmId);
    return vm.update().withoutTag(tagName).apply();
  }

  private VirtualMachine buildVM(VmConfig config) throws Exception {
    LOG.info("Creating instance with config: {}", config);
    WithNetwork withNetwork = azure.virtualMachines()
        .define(config.getName())
        .withRegion(config.getRegion())
        .withExistingResourceGroup(resourceGroup);
    WithOS withOS = networkDecorator.add(withNetwork, config);

    PlatformConfigStrategy configStrategy = config.getPlatformStrategy();
    WithCreate vm = configStrategy.addOS(withOS, config);
    if (config.getOsDiskSize().isPresent()) {
      vm.withOSDiskSizeInGB(config.getOsDiskSize().get().intValue());
    }
    vm = vm.withTags(config.getTags());
    vm = addPlan(vm, config.getImageReference(), config.getRegion());
    return addCustomScriptExtensions(vm, configStrategy.getExtensions(config)).create();
  }

  private WithCreate addPlan(WithCreate vm, ImageReference imageReference, Region region) {
    if (imageReference != null) {
      PurchasePlan plan = azure.virtualMachineImages().getImage(region,
          imageReference.publisher(),
          imageReference.offer(),
          imageReference.sku(),
          imageReference.version()).plan();
      return plan != null ? vm.withPlan(plan) : vm;
    }
    return vm;
  }

  public void terminate(String resourceId) {
    terminate(azure.virtualMachines().getById(resourceId));
  }

  public boolean imageValidForPlatform(ImageURN imageURN, Platform platform, Region region) {
    VirtualMachineImage image = azure.virtualMachineImages().getImage(region,
        imageURN.getPublisher(),
        imageURN.getOffer(),
        imageURN.getSku(),
        imageURN.getVersion());
    return platform.name().equalsIgnoreCase(image.osDiskImage().operatingSystem().name());
  }

  public void terminate(VirtualMachine vm) {
    if (vm != null) {
      ArrayList<String> diskIds = getDiskIds(vm);
      List<String> networkInterfaceIds = vm.networkInterfaceIds();

      LOG.info("Terminating vm {}", vm.name());
      azure.virtualMachines().deleteById(vm.id());

      LOG.info("Terminating vm {} disks", vm.name());
      diskIds.stream()
              .filter(StringUtils::isNotBlank)
              .forEach((id) -> azure.disks().deleteById(id));
      LOG.info("Terminating vm {} nics", vm.name());
      networkInterfaceIds.stream()
              .filter(StringUtils::isNotBlank)
              .forEach((id) -> azure.networkInterfaces().deleteById(id));
    }
  }

  private ArrayList<String> getDiskIds(VirtualMachine vm) {
    ArrayList<String> diskIds = vm.dataDisks()
        .values()
        .stream()
        .map(HasId::id)
        .collect(Collectors.toCollection(ArrayList::new));
    diskIds.add(vm.osDiskId());
    return diskIds;
  }

  private WithCreate addCustomScriptExtensions(WithCreate vm, List<AzureVMExtension> extensions) throws Exception {
    for (AzureVMExtension extension : extensions) {
      vm = extension.addTo(vm);
    }
    return vm;
  }

  public List<VirtualMachine> runningVirtualMachinesWithTag(String tagName, String tagValue) {
    return virtualMachinesWithTag(tagName, tagValue).stream().filter(virtualMachine -> {
      String provisioningState = virtualMachine.provisioningState();
      return isBlank(provisioningState) || !provisioningState.toLowerCase().equals(FAILED_PROVISIONING_STATE);
    }).collect(Collectors.toCollection(ArrayList::new));
  }

  public List<VirtualMachine> failedProvisioningStateVirtualMachinesWithTag(String tagName, String tagValue) {
    return virtualMachinesWithTag(tagName, tagValue).stream().filter(virtualMachine -> {
      String provisioningState = virtualMachine.provisioningState();
      return isNotBlank(provisioningState) && provisioningState.toLowerCase().equals(FAILED_PROVISIONING_STATE);
    }).collect(Collectors.toCollection(ArrayList::new));
  }

  public boolean networkExists(String networkId) {
    try {
      Network network = getNetwork(networkId);
      return network != null;
    } catch (Exception ex) {
      LOG.error("Network {} existence check failed with the following exception {}", networkId, ex);
      return false;
    }
  }

  private Network getNetwork(String networkId) {
    return azure.networks().getById(networkId);
  }

  private List<VirtualMachine> virtualMachinesWithTag(String tagName, String tagValue) {
    return azure.virtualMachines().list()
        .stream()
        .filter(virtualMachine -> tagValue.equals(virtualMachine.tags().get(tagName)))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public boolean subnetExists(String networkId, String subnet) {
    try {
      Network network = getNetwork(networkId);
      return network != null && network.subnets().containsKey(subnet);
    } catch (Exception ex) {
      LOG.error("Subnet {} existence check failed with the following exception {}", subnet, ex);
      return false;
    }
  }

  public boolean networkSecurityGroupExists(String networkSecurityGroupId) {
    try {
      NetworkSecurityGroup nsg = azure.networkSecurityGroups().getById(networkSecurityGroupId);
      return nsg != null;
    } catch (Exception ex) {
      LOG.error("Network Security Group {} existence check failed with the following exception  {}", networkSecurityGroupId, ex);
      return false;
    }
  }

  public boolean resourceGroupExists(String resourceGroup) {
    return azure.resourceGroups().contain(resourceGroup);
  }

  public boolean regionExists(String regionName) {
    PagedList<Location> locations = azure.getCurrentSubscription().listLocations();

    return locations.stream().anyMatch(location -> regionName.equals(location.name()) || regionName.equals(location.displayName()));
  }

  public String runCustomScript(VmConfig config) throws ProvisionFailedException {
    String logs = "";
    try {
      if (StringUtils.isNotBlank(config.getCustomScript())) {
        RunCommandResult runCommandResult = config.getPlatformStrategy().runScript(config.getResourceGroup(), config.getName(), azure.virtualMachines(), config.getCustomScript());
        logs = getLogs(runCommandResult);
        LOG.info("Result of CustomUserScriptExecution on VM {}:", logs);
        runCommandResult.value().forEach(instanceViewStatus -> LOG.info(instanceViewStatus.message()));
      }
    } catch (Exception e) {
      handleScriptExecutionFailure(config.getName(), e, config);
    }
    return logs;
  }

  public void installGoAgent(VmConfig config) throws ProvisionFailedException {
    try {
      LOG.info("Installing GoCD agent on VM {}", config.getName());
      config.getPlatformStrategy().installGoAgent(azure.virtualMachines(), config);
    } catch (Exception e) {
      LOG.error("Failed to install go agent on vm {} due to error: {}\n{} {}", config.getName(), e.getMessage(), e.toString());
      tearDown(config);
      throw new ProvisionFailedException(config.getJobIdentifier(), String.format("Failed to install go agent on %s for job %s due to error: %s", config.getName(), config.getJobIdentifier().getRepresentation(), e.getMessage()));
    }
  }

  private void handleScriptExecutionFailure(String vmName, Exception e, VmConfig config) throws ProvisionFailedException {
    String errorMessage = (e instanceof CloudException) ? ((CloudException)e).body().message() : e.getMessage();
    String message = String.format("Custom user script execution on VM:%s for job: %s failed with error: %s", vmName, config.getJobIdentifier().getRepresentation(), errorMessage);
    LOG.error(message);
    terminate(azure.virtualMachines().getByResourceGroup(resourceGroup, vmName));
    throw new ProvisionFailedException(config.getJobIdentifier(), message);
  }

  public void startAgent(VmConfig config) {
    try {
      LOG.info("About to start GoCD agent on VM {}", config.getName());
      PlatformConfigStrategy configStrategy = config.getPlatformStrategy();
      RunCommandResult runCommandResult = configStrategy.startAgent(config.getResourceGroup(), config.getName(), azure.virtualMachines(), config.getAgentConfig());
      printCommandResult(runCommandResult, format("Logs from go-agent startup on VM %s:", config.getName()));
    } catch (Exception e) {
      LOG.error("Failed to start go-agent VM:{} failed with error: {}", config.getName(), e.getMessage());
      terminate(azure.virtualMachines().getByResourceGroup(resourceGroup, config.getName()));
      throw new RuntimeException(e);
    }
  }

  private void printCommandResult(RunCommandResult runCommandResult, String message) {
    LOG.info(message);
    LOG.info(getLogs(runCommandResult));
  }

  private String getLogs(RunCommandResult runCommandResult) {
    StringBuilder logsBuilder = new StringBuilder();
    runCommandResult.value().forEach(instanceViewStatus -> {
      if (!isBlank(instanceViewStatus.message())) {
        logsBuilder.append(instanceViewStatus.message()).append("\n");
      }
    });
    return logsBuilder.toString();
  }
}
