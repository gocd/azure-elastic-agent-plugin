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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.models.Field;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GetPluginConfigurationExecutorTest {

  @Test
  void shouldSerializeAllFields() {
    GoPluginApiResponse response = new GetPluginConfigurationExecutor().execute();
    Map<String, Field> hashMap = new Gson().fromJson(response.responseBody(), new TypeToken<Map<String, Field>>() {
    }.getType());
    assertEquals(hashMap.size(),
        GetPluginConfigurationExecutor.FIELDS.size(),
        "Are you using anonymous inner classes — see https://github.com/google/gson/issues/298"
    );
  }

  @Test
  void assertJsonStructure() throws Exception {
    GoPluginApiResponse response = new GetPluginConfigurationExecutor().execute();

    assertThat(response.responseCode(), is(200));
    String expectedJSON = "{\n" +
        "  \"go_server_url\": {\n" +
        "    \"display-name\": \"Go Server URL\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"0\"\n" +
        "  },\n" +
        "  \"auto_register_timeout\": {\n" +
        "    \"display-name\": \"Agent auto-register Timeout (in minutes)\",\n" +
        "    \"default-value\": \"10\",\n" +
        "    \"required\": false,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"1\"\n" +
        "  },\n" +
        "  \"idle_timeout\": {\n" +
        "    \"display-name\": \"Agent idle Timeout (in minutes)\",\n" +
        "    \"default-value\": \"0\",\n" +
        "    \"required\": false,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"2\"\n" +
        "  },\n" +
        "  \"resource_group\": {\n" +
        "    \"display-name\": \"Resource Group\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"3\"\n" +
        "  },\n" +
        "  \"region_name\": {\n" +
        "    \"display-name\": \"Region Name\",\n" +
        "    \"default-value\": \"westus\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"4\"\n" +
        "  },\n" +
        "  \"network_id\": {\n" +
        "    \"display-name\": \"Virtual Network Id\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"5\"\n" +
        "  },\n" +
        "  \"subnet\": {\n" +
        "    \"display-name\": \"Subnet Name\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"6\"\n" +
        "  },\n" +
        "  \"network_security_group_id\": {\n" +
        "    \"display-name\": \"Network Security Group Id\",\n" +
        "    \"required\": false,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"7\"\n" +
        "  },\n" +
        "  \"client_id\": {\n" +
        "    \"display-name\": \"Client Id\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"8\"\n" +
        "  },\n" +
        "  \"secret\": {\n" +
        "    \"display-name\": \"Secret\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": true,\n" +
        "    \"display-order\": \"9\"\n" +
        "  },\n" +
        "  \"domain\": {\n" +
        "    \"display-name\": \"Domain/Tenant Id\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"10\"\n" +
        "  },\n" +
        "  \"linux_user_name\": {\n" +
        "    \"display-name\": \"Linux User Name\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"11\"\n" +
        "  },\n" +
        "  \"ssh_key\": {\n" +
        "    \"display-name\": \"Ssh key\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"12\"\n" +
        "  },\n" +
        "  \"windows_user_name\": {\n" +
        "    \"display-name\": \"Windows User Name\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": false,\n" +
        "    \"display-order\": \"13\"\n" +
        "  },\n" +
        "  \"windows_password\": {\n" +
        "    \"display-name\": \"Windows Password\",\n" +
        "    \"required\": true,\n" +
        "    \"secure\": true,\n" +
        "    \"display-order\": \"14\"\n" +
        "  }\n" +
        "}";

    JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);

  }
}
