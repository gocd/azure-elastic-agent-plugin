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
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterface.DefinitionStages.Blank;
import com.microsoft.azure.management.network.NetworkInterface.DefinitionStages.WithGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.vm.VmConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class NetworkDecoratorTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  Azure mockAzure;

  private NetworkDecorator networkDecorator;

  @BeforeEach
  void setUp() {
    openMocks(this);
    networkDecorator = new NetworkDecorator(mockAzure);
  }

  @Test
  void shouldAddNetworkInterfaceWithSecurityGroupAssociatedToTheSubnet() {
    VmConfig vmConfig = mock(VmConfig.class);
    WithNetwork withNetwork = mock(WithNetwork.class, RETURNS_DEEP_STUBS);
    WithOS expectedWithOS = mock(WithOS.class);
    NetworkInterface networkInterface = mock(NetworkInterface.class);
    Network existingNetwork = mock(Network.class);
    NetworkSecurityGroup existingNSG = mock(NetworkSecurityGroup.class);
    WithGroup mockWithGroup = mock(WithGroup.class, RETURNS_DEEP_STUBS);
    Blank mockDefinition = mock(Blank.class);

    when(vmConfig.getRegion()).thenReturn(Region.fromName("azure-region-123"));
    when(vmConfig.getResourceGroup()).thenReturn("resource-group-123");
    when(vmConfig.getSubnet()).thenReturn("subnet-1");
    when(vmConfig.getNetworkId()).thenReturn("network-123");
    when(vmConfig.getNetworkSecurityGroupId()).thenReturn("resources/resource-group-123/network-security-groups/nsg-123");
    when(vmConfig.getNetworkInterfaceName()).thenReturn("nic-vm-123");

    when(mockAzure.networks().getById("network-123")).thenReturn(existingNetwork);
    when(mockAzure.networkSecurityGroups().getById("resources/resource-group-123/network-security-groups/nsg-123"))
        .thenReturn(existingNSG);

    when(mockAzure.networkInterfaces().define("nic-vm-123")).thenReturn(mockDefinition);
    when(mockDefinition.withRegion(Region.fromName("azure-region-123"))).thenReturn(mockWithGroup);
    when(mockWithGroup.withExistingResourceGroup("resource-group-123")
        .withExistingPrimaryNetwork(existingNetwork)
        .withSubnet("subnet-1")
        .withPrimaryPrivateIPAddressDynamic()
        .withExistingNetworkSecurityGroup(existingNSG)
        .create())
        .thenReturn(networkInterface);
    when(withNetwork.withExistingPrimaryNetworkInterface(networkInterface)).thenReturn(expectedWithOS);

    WithOS withOS = networkDecorator.add(withNetwork, vmConfig);

    assertEquals(expectedWithOS, withOS);
  }

  @Test
  void shouldSkipNetworkSecurityGroupIfNotConfigured() {
    assertNSGIsSkippedWhenIDProvidedIs(null);
  }

  @Test
  void shouldSkipNetworkSecurityGroupIfEmpty() {
    assertNSGIsSkippedWhenIDProvidedIs("");
  }

  private void assertNSGIsSkippedWhenIDProvidedIs(String nsgID) {
    VmConfig vmConfig = mock(VmConfig.class);
    WithNetwork withNetwork = mock(WithNetwork.class, RETURNS_DEEP_STUBS);
    WithOS expectedWithOS = mock(WithOS.class);
    NetworkInterface networkInterface = mock(NetworkInterface.class);
    Network existingNetwork = mock(Network.class);
    WithGroup mockWithGroup = mock(WithGroup.class, RETURNS_DEEP_STUBS);
    Blank mockDefinition = mock(Blank.class);

    when(vmConfig.getRegion()).thenReturn(Region.fromName("azure-region-123"));
    when(vmConfig.getResourceGroup()).thenReturn("resource-group-123");
    when(vmConfig.getSubnet()).thenReturn("subnet-1");
    when(vmConfig.getNetworkId()).thenReturn("network-123");
    when(vmConfig.getNetworkInterfaceName()).thenReturn("nic-vm-123");
    when(vmConfig.getNetworkSecurityGroupId()).thenReturn(nsgID);

    when(mockAzure.networks().getById("network-123")).thenReturn(existingNetwork);

    when(mockAzure.networkInterfaces().define("nic-vm-123")).thenReturn(mockDefinition);
    when(mockDefinition.withRegion(Region.fromName("azure-region-123"))).thenReturn(mockWithGroup);
    when(mockWithGroup.withExistingResourceGroup("resource-group-123")
            .withExistingPrimaryNetwork(existingNetwork)
            .withSubnet("subnet-1")
            .withPrimaryPrivateIPAddressDynamic()
            .create())
            .thenReturn(networkInterface);
    when(withNetwork.withExistingPrimaryNetworkInterface(networkInterface)).thenReturn(expectedWithOS);

    WithOS withOS = networkDecorator.add(withNetwork, vmConfig);

    assertEquals(expectedWithOS, withOS);
    verify(mockAzure.networkSecurityGroups(), never()).getByResourceGroup(anyString(), anyString());
  }

}
