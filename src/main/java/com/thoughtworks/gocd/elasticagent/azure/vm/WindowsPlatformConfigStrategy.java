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

package com.thoughtworks.gocd.elasticagent.azure.vm;

import com.microsoft.azure.management.compute.RunCommandResult;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.thoughtworks.gocd.elasticagent.azure.AgentConfig;
import com.thoughtworks.gocd.elasticagent.azure.DownloadUrls;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;

public class WindowsPlatformConfigStrategy implements PlatformConfigStrategy {
  public static final String WINDOWS_START_GO_AGENT_TEMPLATE = "windows_start_go_agent.template.flth";
  public static final String WINDOWS_INSTALL_GO_AGENT_TEMPLATE = "windows_install_go_agent.template.flth";
  private CustomScriptBuilder customScriptBuilder;

  public WindowsPlatformConfigStrategy(CustomScriptBuilder scriptBuilder) {
    this.customScriptBuilder = scriptBuilder;
  }

  @Override
  public VirtualMachine.DefinitionStages.WithCreate addOS(VirtualMachine.DefinitionStages.WithOS withOS, VmConfig config) {
    if (config.getImageReference() == null) {
      return withOS
          .withWindowsCustomImage(config.getCustomImageId())
          .withAdminUsername(config.getUserName())
          .withAdminPassword(config.getPassword())
          .withOSDiskStorageAccountType(config.getOsDiskStorageAccountType())
          .withSize(config.getSize());
    } else {
      return withOS
          .withSpecificWindowsImageVersion(config.getImageReference())
          .withAdminUsername(config.getUserName())
          .withAdminPassword(config.getPassword())
          .withOSDiskStorageAccountType(config.getOsDiskStorageAccountType())
          .withSize(config.getSize());
    }
  }

  @Override
  public List<AzureVMExtension> getExtensions(VmConfig config) {
    return Collections.emptyList();
  }

  @Override
  public RunCommandResult runScript(String resourceGroupName, String vmName, VirtualMachines virtualMachines, String script) {
    return virtualMachines.runPowerShellScript(resourceGroupName, vmName, Collections.singletonList(script), Collections.emptyList());
  }

  @Override
  public RunCommandResult startAgent(String resourceGroup, String name, VirtualMachines virtualMachines, AgentConfig agentConfig) {
    return virtualMachines.runPowerShellScript(resourceGroup, name, Collections.singletonList(startAgentScript()), Collections.emptyList());
  }

  @Override
  public void installGoAgent(VirtualMachines virtualMachines, VmConfig config) {
    RunCommandResult runCommandResult = virtualMachines.runPowerShellScript(config.getResourceGroup(), config.getName(), Collections.singletonList(installGoAgentScript(config)), Collections.emptyList());
    LOG.debug("Result of agent installation script on {}", config.getName());
    runCommandResult.value().forEach(instanceViewStatus -> LOG.debug(instanceViewStatus.message()));
  }

  private String installGoAgentScript(VmConfig config) {
    return customScriptBuilder.withScript(WINDOWS_INSTALL_GO_AGENT_TEMPLATE, agentInstallationParams(config)).build();
  }

  private Map<String, String> agentInstallationParams(VmConfig config) {
    AgentConfig agentConfig = config.getAgentConfig();
    return new HashMap<String, String>() {{
      put("go_server_url", agentConfig.getServerUrl());
      put("autoregister_key", agentConfig.getAutoRegisterKey());
      put("environment", agentConfig.getEnvironment());
      put("plugin_id", Util.pluginId());
      put("agent_id", agentConfig.getAgentId());
      put("username", config.getUserName());
      put("password", config.getPassword());
      put("go_agent_installer_url", DownloadUrls.windowsGoAgent(agentConfig.getVersion()));
    }};
  }

  private String startAgentScript() {
    return customScriptBuilder.withScript(WINDOWS_START_GO_AGENT_TEMPLATE, Collections.emptyMap()).build();
  }

}
