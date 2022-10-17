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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.PluginSettingsNotConfiguredException;
import com.thoughtworks.gocd.elasticagent.azure.requests.ProfileValidateRequest;
import com.thoughtworks.gocd.elasticagent.azure.validations.Validation;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ProfileValidateRequestExecutorTest {
  @Mock
  private GoCDAzureClient mockAzureClient;

  @Mock
  private PluginSettings mockPluginSettings;

  @Mock
  private PluginRequest mockPluginRequest;

  @Mock
  private GoCDAzureClientFactory mockClientFactory;

  @BeforeEach
  void setup() throws Exception {
    openMocks(this);
    when(mockPluginRequest.getPluginSettings()).thenReturn(mockPluginSettings);
    when(mockClientFactory.initialize(mockPluginSettings)).thenReturn(mockAzureClient);
    when(mockPluginSettings.getRegion()).thenReturn(Region.US_WEST);
  }

  @Test
  void shouldShowErrorOnAllFieldsIfPluginSettingsNotConfigured() throws Exception {
    when(mockPluginRequest.getPluginSettings()).thenThrow(PluginSettingsNotConfiguredException.class);

    ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(Collections.emptyMap()), mockPluginRequest, mockClientFactory, Collections.emptyList());
    String json = executor.execute().responseBody();

    JSONAssert.assertEquals("[" +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"idle_timeout\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"vm_image_urn\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"vm_size\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"vm_custom_image_id\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"platform\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"subnet_name\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"os_disk_size\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"os_disk_storage_account_type\"}," +
        "{\"message\":\"Azure plugin settings not configured.\",\"key\":\"custom_script\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  void shouldRunAllValidations() throws JSONException {
    Validation validation1 = mock(Validation.class);
    Validation validation2 = mock(Validation.class);
    List<Validation> validations = Arrays.asList(validation1, validation2);
    Map<String, String> properties = Collections.emptyMap();
    ProfileValidateRequestExecutor executor = new ProfileValidateRequestExecutor(new ProfileValidateRequest(properties), mockPluginRequest, mockClientFactory, validations);
    when(validation1.run(properties, mockPluginSettings, mockAzureClient)).thenReturn(Collections.emptyMap());
    when(validation2.run(properties, mockPluginSettings, mockAzureClient)).thenReturn(Collections.singletonMap("field key", "error message"));

    String json = executor.execute().responseBody();
    JSONAssert.assertEquals("[{\"message\": \"error message\", \"key\": \"field key\"}]", json, JSONCompareMode.NON_EXTENSIBLE);
    verify(validation1).run(properties, mockPluginSettings, mockAzureClient);
    verify(validation2).run(properties, mockPluginSettings, mockAzureClient);
  }
}
