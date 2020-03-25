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
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GetProfileMetadataExecutorTest {

  @Test
  void shouldSerializeAllFields() throws Exception {
    GoPluginApiResponse response = new GetProfileMetadataExecutor().execute();
    List<Metadata> list = new Gson().fromJson(response.responseBody(), new TypeToken<List<Metadata>>() {
    }.getType());
    assertEquals(list.size(), GetProfileMetadataExecutor.FIELDS.size());
  }

  @Test
  void assertJsonStructure() throws Exception {
    GoPluginApiResponse response = new GetProfileMetadataExecutor().execute();

    assertThat(response.responseCode(), is(200));
    String expectedJSON = "[\n" +
        "  {\n" +
        "    \"key\": \"idle_timeout\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": false,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"key\": \"platform\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": true,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"key\": \"vm_image_urn\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": false,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"key\": \"vm_size\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": true,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"key\": \"vm_custom_image_id\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": false,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"key\": \"custom_script\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": false,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"key\": \"os_disk_storage_account_type\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": true,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  }," +
        "  {\n" +
        "    \"key\": \"os_disk_size\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": false,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  }," +
        "  {\n" +
        "    \"key\": \"subnet_name\",\n" +
        "    \"metadata\": {\n" +
        "      \"required\": false,\n" +
        "      \"secure\": false\n" +
        "    }\n" +
        "  }" +
        "]";

    JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
  }

}
