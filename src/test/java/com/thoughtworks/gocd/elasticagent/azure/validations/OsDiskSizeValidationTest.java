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

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsDiskSizeValidationTest {

  private Map<String, String> properties;

  @BeforeEach
  void setUp() {
    properties = new HashMap<>();
  }

  @ParameterizedTest
  @ValueSource(strings = {"45", "126", "1024"})
  void shouldValidateWindowsOSDiskSizeOutside127To1023(String osDiskSize) {
    properties.put("platform", "WINDOWS");
    properties.put("os_disk_size", osDiskSize);

    Map<String, String> errors = new OsDiskSizeValidation().run(properties, null, null);

    assertEquals(1, errors.size());
    assertEquals("OS disk size should be between 127 and 1023.", errors.get("os_disk_size"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"127", "512", "1023"})
  void shouldValidateWindowsOSDiskSizeBetween127And1023(String osDiskSize) throws JSONException {
    properties.put("platform", "WINDOWS");
    properties.put("os_disk_size", osDiskSize);

    Map<String, String> errors = new OsDiskSizeValidation().run(properties, null, null);

    assertTrue(errors.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"10", "29", "1024"})
  void shouldValidateLinuxOSDiskSizeOutside30To1023(String osDiskSize) {
    properties.put("platform", "LINUX");
    properties.put("os_disk_size", osDiskSize);

    Map<String, String> errors = new OsDiskSizeValidation().run(properties, null, null);

    assertEquals(1, errors.size());
    assertEquals("OS disk size should be between 30 and 1023.", errors.get("os_disk_size"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"30", "512", "1023"})
  void shouldValidateWindowsOSDiskSizeBetween20And1023(String osDiskSize) {
    properties.put("platform", "LINUX");
    properties.put("os_disk_size", osDiskSize);

    Map<String, String> errors = new OsDiskSizeValidation().run(properties, null, null);

    assertTrue(errors.isEmpty());
  }

}
