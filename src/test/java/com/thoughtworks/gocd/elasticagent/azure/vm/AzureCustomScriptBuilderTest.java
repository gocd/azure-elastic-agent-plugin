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

import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureCustomScriptBuilderTest extends BaseTest {

  @Test
  void shouldGenerateTheScriptFromTemplateAndDataPassed() {
    Map<String, String> data = new HashMap<>();
    data.put("key", "some-key");
    data.put("value", "some-value");
    String script = new CustomScriptBuilder()
        .withScript("test_template.template.ftlh", data)
        .build();

    assertEquals("key=some-key\n" +
        "value=some-value", script);
  }

  @Test
  void shouldBase64EncodeWhenScriptIsInitialised() {
    String actualScript = "dummy-script";
    CustomScriptBuilder builder = new CustomScriptBuilder().withScript(actualScript);
    String script = builder.base64Encoded().build();

    assertEquals("ZHVtbXktc2NyaXB0", script);
  }

  @Test
  void shouldNotBase64EncodeWhenScriptIsNotInitialised() {
    CustomScriptBuilder builder = new CustomScriptBuilder();
    assertNull(builder.base64Encoded().build());
  }

}
