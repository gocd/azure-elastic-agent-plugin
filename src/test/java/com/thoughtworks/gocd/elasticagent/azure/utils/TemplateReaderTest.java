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

package com.thoughtworks.gocd.elasticagent.azure.utils;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateReaderTest {

  @Test
  void shouldReadTemplateAndPopulatevariables() throws IOException, TemplateException {
    TemplateReader reader = new TemplateReader();
    Map<String, String> data = new HashMap<String, String>() {{
      put("key", "server_url");
      put("value", "https://127.0.0.1/go");
    }};
    String content = reader.read("test_template.template.ftlh", data);

    String expectedContent = "key=server_url\n" + "value=https://127.0.0.1/go";
    assertEquals(expectedContent, content);
  }

}
