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

package com.thoughtworks.gocd.elasticagent.azure;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ProvisionFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import com.thoughtworks.gocd.elasticagent.azure.vm.VmConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.GOCD_SERVER_ID_TAG_KEY;

public class AzureInstanceManager {

  AzureInstanceMapper mapper;
  private final ConcurrentHashMap<String, String> executionLogs = new ConcurrentHashMap<>();


  public AzureInstanceManager(AzureInstanceMapper mapper) {
    this.mapper = mapper;
  }

  public AzureInstance create(GoCDAzureClient client, CreateAgentRequest request, PluginSettings settings, ServerInfo serverInfo) throws ProvisionFailedException {
    VmConfig config = buildVmConfig(request, settings, serverInfo);
    VirtualMachine virtualMachine = client.createVM(config);
    LOG.info("[Instance Manager] Created instance: {}", virtualMachine.name());
    client.installGoAgent(config);
    executeCustomScript(client, config);
    client.startAgent(config);
    LOG.info("[Instance Manager] Started go-agent on instance: {}", virtualMachine.name());
    return mapper.map(virtualMachine);
  }

  private void executeCustomScript(GoCDAzureClient client, VmConfig config) throws ProvisionFailedException {
    String logs = client.runCustomScript(config);
    executionLogs.put(config.getName(), logs);
  }

  public void terminate(GoCDAzureClient client, AzureInstance instance) {
    client.terminate(instance.getId());
  }

  public List<AzureInstance> listInstances(GoCDAzureClient client, String serverId) {
    ArrayList<AzureInstance> instances = new ArrayList<>();
    client.runningVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, serverId).forEach((vm) -> instances.add(mapper.map(vm)));
    return instances;
  }

  public AzureInstance addTag(GoCDAzureClient client, AzureInstance instance, String tagName, String tagValue) {
    VirtualMachine vm = client.addTag(instance.getId(), tagName, tagValue);
    return mapper.map(vm);
  }

  private VmConfig buildVmConfig(CreateAgentRequest request, PluginSettings settings, ServerInfo serverInfo) {
    return new VmConfig.Builder().setRequestParams(request).setSettingsParams(settings).setServerInfoParams(serverInfo).build();
  }

  public AzureInstance removeTag(GoCDAzureClient client, AzureInstance instance, String tagName) {
    return mapper.map(client.removeTag(instance.getId(), tagName));
  }

  public String getExecutionLogs(String instanceName) {
    return executionLogs.get(instanceName);
  }

  public void terminateProvisionFailedVms(GoCDAzureClient goCDAzureClient, String serverId) {
    goCDAzureClient.failedProvisioningStateVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, serverId).stream()
        .forEach(vm -> {
          LOG.info("Terminating VM {} with failed provisioning state", vm.name());
          goCDAzureClient.terminate(vm);
        });
  }
}
