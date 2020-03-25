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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithNetwork;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithOS;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.thoughtworks.gocd.elasticagent.azure.vm.VmConfig;

public class NetworkDecorator {

  private Azure azure;

  public NetworkDecorator(Azure azure) {
    this.azure = azure;
  }

  public WithOS add(WithNetwork withNetwork, VmConfig vmConfig) {
    String nsgId = vmConfig.getNetworkSecurityGroupId();
    NetworkSecurityGroup nsg = (nsgId != null) ?
        azure.networkSecurityGroups().getById(nsgId)
        : null;

    NetworkInterface.DefinitionStages.WithCreate withCreate = azure
        .networkInterfaces()
        .define(vmConfig.getNetworkInterfaceName())
        .withRegion(vmConfig.getRegion())
        .withExistingResourceGroup(vmConfig.getResourceGroup())
        .withExistingPrimaryNetwork(azure.networks().getById(vmConfig.getNetworkId()))
        .withSubnet(vmConfig.getSubnet())
        .withPrimaryPrivateIPAddressDynamic();

    NetworkInterface networkInterface = nsg != null ? withCreate.withExistingNetworkSecurityGroup(nsg).create() : withCreate.create();
    return withNetwork.withExistingPrimaryNetworkInterface(networkInterface);
  }
}
