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

import java.util.List;

public interface PlatformConfigStrategy {
  WithCreate addOS(VirtualMachine.DefinitionStages.WithOS withOS, VmConfig config);

  List<AzureVMExtension> getExtensions(VmConfig config);

  RunCommandResult runScript(String resourceGroupName, String vmName, VirtualMachines virtualMachines, String script);

  RunCommandResult startAgent(String resourceGroup, String name, VirtualMachines virtualMachines, AgentConfig agentConfig);

  void installGoAgent(VirtualMachines virtualMachines, VmConfig config);
}
