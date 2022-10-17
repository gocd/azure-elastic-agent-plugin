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

package com.thoughtworks.gocd.elasticagent.azure.validations;

import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.Errors.*;
import static com.thoughtworks.gocd.elasticagent.azure.executors.GetPluginConfigurationExecutor.*;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AzureNetworkSettingsValidationTest {

  @Mock
  private GoCDAzureClient mockClient;
  private AzureNetworkSettingsValidation validation;

  @BeforeEach
  void setUp() {
    openMocks(this);
    validation = new AzureNetworkSettingsValidation();
  }

  @Test
  void shouldReturnInvalidNetworkIdError() {
    when(mockClient.networkExists("invalid-id")).thenReturn(false);

    Map<String, String> errors = validation.run(singletonMap(NETWORK_ID.key(), "invalid-id"), null, mockClient);

    verify(mockClient, never()).subnetExists(anyString(), anyString());
    verify(mockClient, never()).networkSecurityGroupExists(anyString());
    assertEquals(1, errors.size());
    assertEquals(AZURE_INVALID_NETWORK_ID, errors.get(NETWORK_ID.key()));
  }

  @Test
  void shouldReturnErrorForInvalidSubnetInANetwork() {
    when(mockClient.networkExists("valid-network")).thenReturn(true);
    when(mockClient.subnetExists("valid-network", "invalid-subnet")).thenReturn(false);

    Map<String, String> properties = new HashMap<>();
    properties.put(NETWORK_ID.key(), "valid-network");
    properties.put(SUBNET_NAMES.key(), "invalid-subnet");

    Map<String, String> errors = validation.run(properties, null, mockClient);

    verify(mockClient, never()).networkSecurityGroupExists(anyString());
    assertEquals(1, errors.size());
    assertEquals(String.format(AZURE_INVALID_SUBNET_MESSAGE_FORMAT, "invalid-subnet"), errors.get(SUBNET_NAMES.key()));
  }

  @Test
  void shouldReturnErrorForInvalidNetworkSecurityGroup() {
    when(mockClient.networkExists("valid-network")).thenReturn(true);
    when(mockClient.subnetExists("valid-network", "valid-subnet")).thenReturn(true);
    when(mockClient.networkSecurityGroupExists("invalid-nsg")).thenReturn(false);

    Map<String, String> properties = new HashMap<>();
    properties.put(NETWORK_ID.key(), "valid-network");
    properties.put(SUBNET_NAMES.key(), "valid-subnet");
    properties.put(NETWORK_SECURITY_GROUP_ID.key(), "invalid-nsg");

    Map<String, String> errors = validation.run(properties, null, mockClient);

    assertEquals(1, errors.size());
    assertEquals(AZURE_INVALID_NSG_ID, errors.get(NETWORK_SECURITY_GROUP_ID.key()));
  }


  @Test
  void shouldValidateForNetworkSecurityGroupOnlyWhenProvided() {
    when(mockClient.networkExists("valid-network")).thenReturn(true);
    when(mockClient.subnetExists("valid-network", "valid-subnet")).thenReturn(true);

    Map<String, String> properties = new HashMap<>();
    properties.put(NETWORK_ID.key(), "valid-network");
    properties.put(SUBNET_NAMES.key(), "valid-subnet");

    Map<String, String> errors = validation.run(properties, null, mockClient);

    verify(mockClient, never()).networkSecurityGroupExists(anyString());
    assertTrue(errors.isEmpty());
  }

  @Test
  void shouldReturnNoErrorForValidNetworkSettings() {
    InOrder inOrder = inOrder(mockClient);

    when(mockClient.networkExists("valid-network")).thenReturn(true);
    when(mockClient.subnetExists("valid-network", "valid-subnet")).thenReturn(true);
    when(mockClient.networkSecurityGroupExists("valid-nsg")).thenReturn(true);

    Map<String, String> properties = new HashMap<>();
    properties.put(NETWORK_ID.key(), "valid-network");
    properties.put(SUBNET_NAMES.key(), "valid-subnet");
    properties.put(NETWORK_SECURITY_GROUP_ID.key(), "valid-nsg");
    Map<String, String> errors = validation.run(properties, null, mockClient);

    assertTrue(errors.isEmpty());
    inOrder.verify(mockClient).networkExists("valid-network");
    inOrder.verify(mockClient).subnetExists("valid-network", "valid-subnet");
    inOrder.verify(mockClient).networkSecurityGroupExists("valid-nsg");
  }
}
