/*
 * Copyright 2020 ThoughtWorks, Inc.
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

import com.microsoft.azure.CloudError;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.*;
import com.microsoft.azure.management.compute.VirtualMachine.Update;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.AgentConfig;
import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ProvisionFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.ImageURN;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import com.thoughtworks.gocd.elasticagent.azure.vm.AzureVMExtension;
import com.thoughtworks.gocd.elasticagent.azure.vm.PlatformConfigStrategy;
import com.thoughtworks.gocd.elasticagent.azure.vm.VmConfig;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import retrofit2.Response;

import java.security.InvalidParameterException;
import java.util.*;

import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.GOCD_SERVER_ID_TAG_KEY;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class GoCDAzureClientTest extends BaseTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Azure azure;

  @Mock
  private NetworkDecorator mockNetworkDecorator;

  private GoCDAzureClient goCDAzureClient;
  private String resourceGroup = "AGENTS-group";

  @BeforeEach
  void setUp() {
    openMocks(this);
    goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);

  }

  @Test
  void testShouldFetchRunningVirtualMachinesInResourceGroupWithCaseInsensitivityAndWithServerIdTag() {
    VirtualMachine vm1 = mock(VirtualMachine.class);
    VirtualMachine vm2 = mock(VirtualMachine.class);
    VirtualMachine vm3 = mock(VirtualMachine.class);

    when(vm1.resourceGroupName()).thenReturn("agents-group");
    Map<String, String> tagsMap = new HashMap<String, String>() {{
      put(GOCD_SERVER_ID_TAG_KEY, "server_id");
    }};
    when(vm1.tags()).thenReturn(tagsMap);
    when(vm2.resourceGroupName()).thenReturn("another-group");
    when(vm3.resourceGroupName()).thenReturn("agents-group");
    when(vm3.tags()).thenReturn(tagsMap);
    List<VirtualMachine> machines = asList(vm1, vm2, vm3);
    when(azure.virtualMachines().list().stream()).thenReturn(machines.stream()).thenReturn(machines.stream());
    List<VirtualMachine> vms = new GoCDAzureClient(azure, "agents-group", mockNetworkDecorator).runningVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, "server_id");

    assertEquals(2, vms.size());
    assertEquals(vm1, vms.get(0));
    assertEquals(vm3, vms.get(1));

    vms = new GoCDAzureClient(azure, "AGENTS-group", mockNetworkDecorator).runningVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, "server_id");

    assertEquals(2, vms.size());
    assertEquals(vm1, vms.get(0));
    assertEquals(vm3, vms.get(1));
  }

  @Test
  void testRunningVirtualMachinesShouldNotFetchVmsWithFailedProvisioningState() {
    VirtualMachine runningVm = mock(VirtualMachine.class);
    VirtualMachine failedVm = mock(VirtualMachine.class);

    Map<String, String> tagsMap = new HashMap<String, String>() {{
      put(GOCD_SERVER_ID_TAG_KEY, "server_id");
    }};
    when(runningVm.resourceGroupName()).thenReturn("agents-group");
    when(runningVm.tags()).thenReturn(tagsMap);
    when(runningVm.provisioningState()).thenReturn("Succeeded");
    when(failedVm.resourceGroupName()).thenReturn("agents-group");
    when(failedVm.provisioningState()).thenReturn("Failed");
    when(failedVm.tags()).thenReturn(tagsMap);
    List<VirtualMachine> machines = asList(runningVm, failedVm);
    when(azure.virtualMachines().list().stream()).thenReturn(machines.stream()).thenReturn(machines.stream());
    List<VirtualMachine> vms = new GoCDAzureClient(azure, "agents-group", mockNetworkDecorator).runningVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, "server_id");

    assertEquals(1, vms.size());
    assertEquals(runningVm, vms.get(0));
  }

  @Test
  void testShouldFetchVmsWithFailedProvisioningState() {
    VirtualMachine runningVm = mock(VirtualMachine.class);
    VirtualMachine failedVm = mock(VirtualMachine.class);

    Map<String, String> tagsMap = new HashMap<String, String>() {{
      put(GOCD_SERVER_ID_TAG_KEY, "server_id");
    }};
    when(runningVm.resourceGroupName()).thenReturn("agents-group");
    when(runningVm.tags()).thenReturn(tagsMap);
    when(runningVm.provisioningState()).thenReturn("Succeeded");
    when(failedVm.resourceGroupName()).thenReturn("agents-group");
    when(failedVm.provisioningState()).thenReturn("Failed");
    when(failedVm.tags()).thenReturn(tagsMap);
    List<VirtualMachine> machines = asList(runningVm, failedVm);
    when(azure.virtualMachines().list().stream()).thenReturn(machines.stream()).thenReturn(machines.stream());
    List<VirtualMachine> vms = new GoCDAzureClient(azure, "agents-group", mockNetworkDecorator).failedProvisioningStateVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, "server_id");

    assertEquals(1, vms.size());
    assertEquals(failedVm, vms.get(0));
  }

  @Test
  void shouldReturnFalseIfNetworkDoesnotExist() {
    when(azure.networks().getById("invalid-network-id")).thenReturn(null);

    assertFalse(goCDAzureClient.networkExists("invalid-network-id"));
  }

  @Test
  void shouldReturnTrueIfNetworkExist() {
    when(azure.networks().getById("invalid-network-id")).thenReturn(mock(Network.class));

    assertTrue(goCDAzureClient.networkExists("invalid-network-id"));
  }

  @Test
  void shouldCheckForInvalidNetworkId() {
    when(azure.networks().getById("some-invalid-format")).thenThrow(InvalidParameterException.class);

    assertFalse(goCDAzureClient.networkExists("some-invalid-format"));
  }

  @Test
  void shouldCheckIfResourceGroupExists() {
    when(azure.resourceGroups().contain("some-resource-group")).thenReturn(true);

    assertTrue(goCDAzureClient.resourceGroupExists("some-resource-group"));
  }

  @Test
  void shouldCheckIfRegionExistsByName() {
    Location location1 = mock(Location.class);
    when(location1.name()).thenReturn("valid-region-1");
    Location location2 = mock(Location.class);
    when(location2.name()).thenReturn("valid-region-2");
    List<Location> locations = asList(location1, location2);

    when(azure.getCurrentSubscription().listLocations().stream()).thenReturn(locations.stream()).thenReturn(locations.stream());

    assertTrue(goCDAzureClient.regionExists("valid-region-1"));
    assertFalse(goCDAzureClient.regionExists("invalid-region"));
  }

  @Test
  void shouldCheckIfRegionExistsByDisplayName() {
    Location location1 = mock(Location.class);
    when(location1.displayName()).thenReturn("Valid Region 1");
    Location location2 = mock(Location.class);
    when(location2.displayName()).thenReturn("Valid Region 2");
    List<Location> locations = asList(location1, location2);

    when(azure.getCurrentSubscription().listLocations().stream()).thenReturn(locations.stream()).thenReturn(locations.stream());

    assertTrue(goCDAzureClient.regionExists("Valid Region 1"));
    assertFalse(goCDAzureClient.regionExists("Invalid Region"));
  }

  @Test
  void shouldCheckIfNetworkSecurityGroupExists() {
    when(azure.networkSecurityGroups().getById("valid-security-group-id"))
        .thenReturn(mock(NetworkSecurityGroup.class));
    when(azure.networkSecurityGroups().getById("invalid-security-group-id"))
        .thenReturn(null);

    assertTrue(goCDAzureClient.networkSecurityGroupExists("valid-security-group-id"));
    assertFalse(goCDAzureClient.networkSecurityGroupExists("invalid-security-group-id"));
  }

  @Test
  void shouldCheckIfSubnetExistsInAGivenNetwork() {
    Network mockNetwork = mock(Network.class, RETURNS_DEEP_STUBS);
    when(azure.networks().getById("rg/test-net")).thenReturn(mockNetwork);
    when(mockNetwork.subnets().containsKey("test-subnet")).thenReturn(true);

    assertTrue(goCDAzureClient.subnetExists("rg/test-net", "test-subnet"));
  }

  @Test
  void shouldReturnSubnetDoesnotExistForAnInvalidNetwork() {
    when(azure.networks().getById("rg/test-net")).thenReturn(null);

    assertFalse(goCDAzureClient.subnetExists("rg/test-net", "test-subnet"));
  }

  @Test
  void shouldReturnSubnetDoesnotExistForAnInvalidSubnetInANetwork() {
    Network mockNetwork = mock(Network.class, RETURNS_DEEP_STUBS);
    when(mockNetwork.subnets().containsKey("test-subnet")).thenReturn(false);
    when(azure.networks().getById("rg/test-net")).thenReturn(mockNetwork);

    assertFalse(goCDAzureClient.subnetExists("rg/test-net", "test-subnet"));
  }

  @Test
  void testBuildVmShouldAddPlatformSpecificOSWithoutPlan() throws Exception {
    VmConfig mockVMConfig = mock(VmConfig.class, RETURNS_DEEP_STUBS);
    Region region = mock(Region.class, RETURNS_DEEP_STUBS);
    WithOS mockOS = mock(WithOS.class);
    WithGroup mockWithGroup = mock(WithGroup.class);
    Blank mockStage = mock(Blank.class, RETURNS_DEEP_STUBS);
    WithNetwork mockWithNetwork = mock(WithNetwork.class, RETURNS_DEEP_STUBS);
    WithCreate mockWithCreate = mock(WithCreate.class, RETURNS_DEEP_STUBS);
    PlatformConfigStrategy mockStrategy = mock(PlatformConfigStrategy.class);
    AzureVMExtension mockExtension = mock(AzureVMExtension.class);
    Map<String, String> tags = new HashMap<>();
    List<AzureVMExtension> extensions = asList(mockExtension);
    VirtualMachine expectedVM = mock(VirtualMachine.class);

    when(mockVMConfig.getRegion()).thenReturn(region);
    when(mockVMConfig.getImageReference()).thenReturn(null);
    when(mockVMConfig.getName()).thenReturn("vm-name");
    when(mockVMConfig.getPlatformStrategy()).thenReturn(mockStrategy);
    when(mockVMConfig.getTags()).thenReturn(tags);
    when(mockVMConfig.getOsDiskSize()).thenReturn(Optional.of(45));
    when(azure.virtualMachines().define("vm-name")).thenReturn(mockStage);
    when(mockStage.withRegion(region)).thenReturn(mockWithGroup);
    when(mockWithGroup.withExistingResourceGroup(resourceGroup)).thenReturn(mockWithNetwork);
    when(mockNetworkDecorator.add(mockWithNetwork, mockVMConfig)).thenReturn(mockOS);
    when(mockStrategy.addOS(mockOS, mockVMConfig)).thenReturn(mockWithCreate);
    when(mockStrategy.getExtensions(mockVMConfig)).thenReturn(extensions);
    when(mockWithCreate.withTags(tags)).thenReturn(mockWithCreate);

    when(mockExtension.addTo(mockWithCreate)).thenReturn(mockWithCreate);
    when(mockWithCreate.create()).thenReturn(expectedVM);

    VirtualMachine createdVM = goCDAzureClient.createVM(mockVMConfig);

    verify(mockStrategy).addOS(mockOS, mockVMConfig);
    verify(mockWithCreate).withTags(tags);
    verify(mockWithCreate, never()).withPlan(any());
    verify(mockExtension).addTo(mockWithCreate);
    verify(mockWithCreate).withOSDiskSizeInGB(45);
    verify(mockWithCreate).create();

    assertEquals(expectedVM, createdVM);
  }

  @Test
  void testRunCommandShouldDoNothingForBlankScript() throws Exception {
    VmConfig mockVMConfig = mock(VmConfig.class, RETURNS_DEEP_STUBS);
    when(mockVMConfig.getCustomScript()).thenReturn(" ");

    goCDAzureClient.runCustomScript(mockVMConfig);

    verify(azure, never()).virtualMachines();
  }

  @Test
  void testRunCommandShouldInvokeCommandAndGenerateLogs() throws Exception {
    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, "AGENTS", mockNetworkDecorator);
    VmConfig mockVMConfig = mock(VmConfig.class, RETURNS_DEEP_STUBS);
    PlatformConfigStrategy mockStrategy = mock(PlatformConfigStrategy.class, RETURNS_DEEP_STUBS);
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    when(mockVMConfig.getPlatformStrategy()).thenReturn(mockStrategy);
    when(mockVMConfig.getName()).thenReturn("vmName");
    when(mockVMConfig.getResourceGroup()).thenReturn("AGENTS");
    when(mockVMConfig.getCustomScript()).thenReturn("custom script");
    when(azure.virtualMachines()).thenReturn(mockVirtualMachines);

    RunCommandResult runCommandResult = mock(RunCommandResult.class);
    InstanceViewStatus instanceViewStatus1 = mock(InstanceViewStatus.class);
    InstanceViewStatus instanceViewStatus2 = mock(InstanceViewStatus.class);
    when(instanceViewStatus1.message()).thenReturn("logs 1");
    when(instanceViewStatus2.message()).thenReturn("logs 2");
    when(runCommandResult.value()).thenReturn(Arrays.asList(instanceViewStatus1, instanceViewStatus2));
    when(mockStrategy.runScript("AGENTS", "vmName", mockVirtualMachines, "custom script")).thenReturn(runCommandResult);

    String logs = goCDAzureClient.runCustomScript(mockVMConfig);

    assertEquals("logs 1\nlogs 2\n", logs);
    verify(azure).virtualMachines();
    verify(mockStrategy).runScript("AGENTS", "vmName", mockVirtualMachines, "custom script");
  }

  @Test
  void testStartAgentShouldInvokeCommand() {
    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);
    VmConfig mockVMConfig = mock(VmConfig.class, RETURNS_DEEP_STUBS);
    AgentConfig agentConfig = mock(AgentConfig.class);
    PlatformConfigStrategy mockStrategy = mock(PlatformConfigStrategy.class, RETURNS_DEEP_STUBS);
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    when(mockVMConfig.getPlatformStrategy()).thenReturn(mockStrategy);
    when(mockVMConfig.getName()).thenReturn("vmName");
    when(mockVMConfig.getResourceGroup()).thenReturn("AGENTS");
    when(mockVMConfig.getAgentConfig()).thenReturn(agentConfig);
    when(azure.virtualMachines()).thenReturn(mockVirtualMachines);

    goCDAzureClient.startAgent(mockVMConfig);

    verify(azure).virtualMachines();
    verify(mockStrategy).startAgent("AGENTS", "vmName", mockVirtualMachines, agentConfig);
  }

  @Test
  void testInstallAgentShouldInvokeCommand() throws ProvisionFailedException {
    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);
    VmConfig mockVMConfig = mock(VmConfig.class, RETURNS_DEEP_STUBS);
    PlatformConfigStrategy mockStrategy = mock(PlatformConfigStrategy.class, RETURNS_DEEP_STUBS);
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    when(mockVMConfig.getPlatformStrategy()).thenReturn(mockStrategy);
    when(azure.virtualMachines()).thenReturn(mockVirtualMachines);

    goCDAzureClient.installGoAgent(mockVMConfig);

    verify(azure).virtualMachines();
    verify(mockStrategy).installGoAgent(mockVirtualMachines, mockVMConfig);
  }

  @Test
  void testShouldReRaiseExceptionAndTerminateVMOnCommandExecutionFailure() {
    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);
    VmConfig mockVMConfig = mock(VmConfig.class, RETURNS_DEEP_STUBS);
    VirtualMachine mockVM = mock(VirtualMachine.class);
    PlatformConfigStrategy mockStrategy = mock(PlatformConfigStrategy.class);

    when(mockVMConfig.getCustomScript()).thenReturn("custom script");
    when(mockVMConfig.getPlatformStrategy()).thenReturn(mockStrategy);
    when(mockVMConfig.getResourceGroup()).thenReturn(resourceGroup);
    when(mockVMConfig.getName()).thenReturn("Test-123");
    when(mockVM.id()).thenReturn("/resources/Test-123");
    when(azure.virtualMachines().getByResourceGroup(resourceGroup, "Test-123")).thenReturn(mockVM);
    when(mockStrategy.runScript(eq(resourceGroup), eq("Test-123"), any(), eq("custom script")))
        .thenThrow(new CloudException("script failed",
            Response.error(500, ResponseBody.create(MediaType.parse("application/json"), "boom")),
            new CloudError()));

    assertThrows(ProvisionFailedException.class, () -> goCDAzureClient.runCustomScript(mockVMConfig));
    verify(azure.virtualMachines()).deleteById("/resources/Test-123");
  }

  @Test
  void shouldAddTagToTheVirtualMachine() {
    VirtualMachine mockVM = mock(VirtualMachine.class);
    VirtualMachine mockVMWithTags = mock(VirtualMachine.class);
    Update mockUpdate = mock(Update.class, RETURNS_DEEP_STUBS);
    when(mockVM.update()).thenReturn(mockUpdate);
    when(azure.virtualMachines().getById("vm-id")).thenReturn(mockVM);
    when(mockUpdate.withTag("tag-1", "value-1").apply()).thenReturn(mockVMWithTags);

    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);

    VirtualMachine vmWithTags = goCDAzureClient.addTag("vm-id", "tag-1", "value-1");

    verify(mockUpdate.withTag("tag-1", "value-1"), times(1)).apply();
    assertEquals(mockVMWithTags, vmWithTags);
  }

  @Test
  void shouldRemoveTagFromVirtualMachine() {
    VirtualMachine mockVM = mock(VirtualMachine.class);
    VirtualMachine mockVMWithoutTag = mock(VirtualMachine.class);
    Update mockUpdate = mock(Update.class, RETURNS_DEEP_STUBS);
    when(mockVM.update()).thenReturn(mockUpdate);
    when(azure.virtualMachines().getById("vm-id")).thenReturn(mockVM);
    when(mockUpdate.withoutTag("tag-1").apply()).thenReturn(mockVMWithoutTag);

    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);

    VirtualMachine vmWithoutTags = goCDAzureClient.removeTag("vm-id", "tag-1");

    verify(mockUpdate.withoutTag("tag-1"), times(1)).apply();
    assertEquals(mockVMWithoutTag, vmWithoutTags);
  }

  @Test
  void shouldCleanupNICOnVMCreationFailure() {
    VmConfig mockVMConfig = mock(VmConfig.class, RETURNS_DEEP_STUBS);
    NetworkInterface mockNic = mock(NetworkInterface.class);
    when(mockVMConfig.getName()).thenReturn("vm-123");
    when(mockVMConfig.getNetworkInterfaceName()).thenReturn("nic-vm-123");
    when(mockVMConfig.getJobIdentifier().getRepresentation()).thenReturn("jobid");
    when(azure.virtualMachines().define("vm-123")).thenThrow(new RuntimeException("boom"));
    when(azure.virtualMachines().getByResourceGroup(anyString(), anyString())).thenReturn(null);
    when(azure.networkInterfaces().getByResourceGroup(resourceGroup, "nic-vm-123")).thenReturn(mockNic);
    when(mockNic.id()).thenReturn("nicId");

    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);

    ProvisionFailedException exception = assertThrows(ProvisionFailedException.class, () -> goCDAzureClient.createVM(mockVMConfig));
    assertEquals("Failed to create vm with name vm-123 for job jobid due to error: boom", exception.getMessage());

    verify(azure.networkInterfaces()).getByResourceGroup(resourceGroup, "nic-vm-123");
    verify(azure.networkInterfaces()).deleteById("nicId");
  }

  @Test
  void shouldValidateTheImageURNChosenIsCorrespondingToThePlatformSelected() {
    VirtualMachineImage mockVMImage = mock(VirtualMachineImage.class);
    OSDiskImage mockDiskImage = mock(OSDiskImage.class);

    when(mockDiskImage.operatingSystem()).thenReturn(OperatingSystemTypes.LINUX);
    when(mockVMImage.osDiskImage()).thenReturn(mockDiskImage);
    when(azure.virtualMachineImages().getImage(Region.INDIA_SOUTH, "pub-1", "offer-1", "sku-1", "version-1")).thenReturn(mockVMImage);

    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);

    assertFalse(goCDAzureClient.imageValidForPlatform(new ImageURN("pub-1:offer-1:sku-1:version-1"), Platform.WINDOWS, Region.INDIA_SOUTH));
    assertTrue(goCDAzureClient.imageValidForPlatform(new ImageURN("pub-1:offer-1:sku-1:version-1"), Platform.LINUX, Region.INDIA_SOUTH));

  }

  @Test
  void shouldTerminateVMAlongWithAssociatedResources() {
    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);

    VirtualMachine vm = mock(VirtualMachine.class);
    when(azure.virtualMachines().getById("resource-id-to-be-deleted")).thenReturn(vm);
    when(vm.id()).thenReturn("resource-id-to-be-deleted");
    when(vm.osDiskId()).thenReturn("os-disk-id");
    when(vm.dataDisks()).thenReturn(Collections.emptyMap());
    when(vm.networkInterfaceIds()).thenReturn(asList("nic-1"));

    goCDAzureClient.terminate("resource-id-to-be-deleted");
    verify(azure.virtualMachines()).deleteById("resource-id-to-be-deleted");
    verify(azure.disks()).deleteById("os-disk-id");
    verify(azure.networkInterfaces()).deleteById("nic-1");
  }

  @Test
  void shouldTerminateVMIfNICAndDiskIsNotAttached() {
    GoCDAzureClient goCDAzureClient = new GoCDAzureClient(azure, resourceGroup, mockNetworkDecorator);

    VirtualMachine vm = mock(VirtualMachine.class);
    when(azure.virtualMachines().getById("resource-id-to-be-deleted")).thenReturn(vm);
    when(vm.id()).thenReturn("resource-id-to-be-deleted");
    when(vm.osDiskId()).thenReturn(null);
    when(vm.dataDisks()).thenReturn(Collections.emptyMap());
    when(vm.networkInterfaceIds()).thenReturn(asList(""));

    goCDAzureClient.terminate("resource-id-to-be-deleted");
    verify(azure.virtualMachines()).deleteById("resource-id-to-be-deleted");
    verify(azure.disks(), never()).deleteById(null);
    verify(azure.networkInterfaces(), never()).deleteById(null);
  }

}
