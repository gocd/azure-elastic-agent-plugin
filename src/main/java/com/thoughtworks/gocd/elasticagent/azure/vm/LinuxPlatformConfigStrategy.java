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

package com.thoughtworks.gocd.elasticagent.azure.vm;

import com.microsoft.azure.management.compute.RunCommandResult;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithCreate;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.thoughtworks.gocd.elasticagent.azure.AgentConfig;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LinuxPlatformConfigStrategy implements PlatformConfigStrategy {
  public static final String LINUX_START_GO_AGENT_TEMPLATE = "linux_start_go_agent.template.flth";
  private CustomScriptBuilder customScriptBuilder;

  public LinuxPlatformConfigStrategy(CustomScriptBuilder customScriptBuilder) {
    this.customScriptBuilder = customScriptBuilder;
  }

  @Override
  public WithCreate addOS(VirtualMachine.DefinitionStages.WithOS withOS, VmConfig config) {
    if (config.getImageReference() == null) {
      return withOS
          .withLinuxCustomImage(config.getCustomImageId())
          .withRootUsername(config.getUserName())
          .withSsh(config.getSshKey())
          .withOSDiskStorageAccountType(config.getOsDiskStorageAccountType())
          .withSize(config.getSize());
    } else {
      return withOS
          .withSpecificLinuxImageVersion(config.getImageReference())
          .withRootUsername(config.getUserName())
          .withSsh(config.getSshKey())
          .withOSDiskStorageAccountType(config.getOsDiskStorageAccountType())
          .withSize(config.getSize());
    }
  }

  @Override
  public List<AzureVMExtension> getExtensions(VmConfig config) {
    AgentConfig agentConfig = config.getAgentConfig();
    return Arrays.asList(new LinuxCustomScriptExtension(agentConfig.getVersion(),
        agentConfig.getServerUrl(),
        agentConfig.getAutoRegisterKey(),
        config.getEnvironment(),
        Util.pluginId(),
        agentConfig.getAgentId()
    ));
  }

  @Override
  public RunCommandResult runScript(String resourceGroupName, String vmName, VirtualMachines virtualMachines, String script) {
    return virtualMachines.runShellScript(resourceGroupName, vmName, Collections.singletonList(script), Collections.emptyList());
  }

  @Override
  public RunCommandResult startAgent(String resourceGroup, String name, VirtualMachines virtualMachines, AgentConfig agentConfig) {
    return virtualMachines.runShellScript(resourceGroup, name, Collections.singletonList(startGoAgentScript(agentConfig)), Collections.emptyList());
  }

  @Override
  public void installGoAgent(VirtualMachines virtualMachines, VmConfig config) {
    // Agent installed in custom script extension for Linux
  }

  private String startGoAgentScript(AgentConfig agentConfig) {
    return customScriptBuilder.withScript(LINUX_START_GO_AGENT_TEMPLATE, Collections.singletonMap("go_server_url", agentConfig.getServerUrl())).build();
  }

}
