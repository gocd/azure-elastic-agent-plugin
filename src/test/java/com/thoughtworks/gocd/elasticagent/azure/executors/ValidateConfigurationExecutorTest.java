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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.validations.FieldValidation;
import com.thoughtworks.gocd.elasticagent.azure.validations.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

class ValidateConfigurationExecutorTest {

  @Mock
  private GoCDAzureClientFactory clientFactory;

  @Mock
  private GoCDAzureClient mockClient;
  private Map<String, String> settings;
  private ValidateConfigurationExecutor validateConfigurationExecutor;

  @BeforeEach
  void setUp() {
    openMocks(this);
    settings = new HashMap<>();
  }

  @Test
  void shouldValidateABadConfiguration() throws Exception {
    validateConfigurationExecutor = new ValidateConfigurationExecutor(settings, clientFactory, Collections.singletonList(new FieldValidation()));
    GoPluginApiResponse response = validateConfigurationExecutor.execute();

    assertThat(response.responseCode(), is(200));
    JSONAssert.assertEquals("[\n" +
        "  {\n" +
        "    \"message\": \"Go Server URL must not be blank.\",\n" +
        "    \"key\": \"go_server_url\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Agent auto-register Timeout (in minutes) must be a positive integer.\",\n" +
        "    \"key\": \"auto_register_timeout\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Linux User Name must not be blank.\",\n" +
        "    \"key\": \"linux_user_name\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Ssh key must not be blank.\",\n" +
        "    \"key\": \"ssh_key\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Windows User Name must not be blank.\",\n" +
        "    \"key\": \"windows_user_name\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Windows Password must not be blank.\",\n" +
        "    \"key\": \"windows_password\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Domain/Tenant Id must not be blank.\",\n" +
        "    \"key\": \"domain\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Client Id must not be blank.\",\n" +
        "    \"key\": \"client_id\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Secret must not be blank.\",\n" +
        "    \"key\": \"secret\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Resource Group must not be blank.\",\n" +
        "    \"key\": \"resource_group\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Region Name must not be blank.\",\n" +
        "    \"key\": \"region_name\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Virtual Network Id must not be blank.\",\n" +
        "    \"key\": \"network_id\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Subnet Name must not be blank.\",\n" +
        "    \"key\": \"subnet\"\n" +
        "  }\n" +
        "]", response.responseBody(), NON_EXTENSIBLE);
  }

  @Test
  void shouldRunAllValidations() throws Exception {
    Validation validation1 = mock(Validation.class);
    Validation validation2 = mock(Validation.class);
    validateConfigurationExecutor = new ValidateConfigurationExecutor(settings, clientFactory, Arrays.asList(validation1, validation2));
    when(clientFactory.initialize(any(), any(), any(), any(), any())).thenReturn(mockClient);
    when(validation2.run(settings, mockClient)).thenReturn(Collections.singletonMap("key", "error message"));

    GoPluginApiResponse response = validateConfigurationExecutor.execute();

    assertThat(response.responseCode(), is(200));
    verify(validation1).run(settings, mockClient);
    verify(validation2).run(settings, mockClient);
    JSONAssert.assertEquals("[{\"key\": \"key\", \"message\": \"error message\"}]", response.responseBody(), NON_EXTENSIBLE);
  }

  @Test
  void shouldReturnAuthenticationErrorsWhenAzureCredentialsAreInvalid() throws Exception {
    when(clientFactory.initialize(any(), any(), any(), any(), any())).thenThrow(RuntimeException.class);
    GoPluginApiResponse response = new ValidateConfigurationExecutor(Collections.emptyMap(), clientFactory, Collections.emptyList()).execute();

    assertEquals(200, response.responseCode());


    JSONAssert.assertEquals("[\n" +
        "  {\n" +
        "    \"message\": \"Invalid Azure auth credentials\",\n" +
        "    \"key\": \"domain\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Invalid Azure auth credentials\",\n" +
        "    \"key\": \"secret\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"message\": \"Invalid Azure auth credentials\",\n" +
        "    \"key\": \"client_id\"\n" +
        "  }]", response.responseBody(), true);

  }

}
