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
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.Errors.AZURE_INVALID_RESOURCE_GROUP;
import static com.thoughtworks.gocd.elasticagent.azure.executors.GetPluginConfigurationExecutor.RESOURCE_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class AzureResourceGroupValidationTest {

  @Mock
  private GoCDAzureClient mockClient;
  private AzureResourceGroupValidation validation;

  @BeforeEach
  void setUp() {
    openMocks(this);
    validation = new AzureResourceGroupValidation();
  }

  @Test
  void shouldReturnErrorForInvalidResourceGroup() {
    String resourceGroup = "non-existing-resource-group";
    when(mockClient.resourceGroupExists(resourceGroup)).thenReturn(false);

    Map<String, String> errors = validation.run(Collections.singletonMap(RESOURCE_GROUP.key(), resourceGroup), null, mockClient);
    assertEquals(1, errors.size());

    assertEquals(AZURE_INVALID_RESOURCE_GROUP, errors.get(RESOURCE_GROUP.key()));
  }

  @Test
  void shouldNotReturnErrorForValidResourceGroup() {
    String resourceGroup = "existing-resource-group";
    when(mockClient.resourceGroupExists(resourceGroup)).thenReturn(true);

    Map<String, String> errors = validation.run(Collections.singletonMap(RESOURCE_GROUP.key(), resourceGroup), null, mockClient);
    assertTrue(errors.isEmpty());
  }

}
