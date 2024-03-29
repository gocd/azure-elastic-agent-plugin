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

package com.thoughtworks.gocd.elasticagent.azure.requests;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ValidatePluginSettingsTest {

  @Test
  void shouldDeserializeFromJSON() {
    String json = "{\n" +
        "  \"plugin-settings\": {\n" +
        "    \"server_url\": {\n" +
        "      \"value\": \"http://localhost\"\n" +
        "    },\n" +
        "    \"username\": {\n" +
        "      \"value\": \"bob\"\n" +
        "    },\n" +
        "    \"password\": {\n" +
        "      \"value\": \"secret\"\n" +
        "    }\n" +
        "  }\n" +
        "}";

    ValidatePluginSettings request = ValidatePluginSettings.fromJSON(json);
    HashMap<String, String> expectedSettings = new HashMap<>();
    expectedSettings.put("server_url", "http://localhost");
    expectedSettings.put("username", "bob");
    expectedSettings.put("password", "secret");
    assertThat(request, equalTo(expectedSettings));
  }
}
