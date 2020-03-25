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

package com.thoughtworks.gocd.elasticagent.azure.validations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsDiskStorageTypeValidationTest {

  @Test
  void shouldValidateOsDiskStorageTypeIsInvalid() {
    Map<String, String> properties = Collections.singletonMap("os_disk_storage_account_type", "invalid");

    Map<String, String> errors = new OsDiskStorageTypeValidation().run(properties, null, null);

    assertEquals(1, errors.size());
    assertEquals("Supported OS disk storage types: Premium_LRS, StandardSSD_LRS, Standard_LRS.", errors.get("os_disk_storage_account_type"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Standard_LRS", "Premium_LRS", "StandardSSD_LRS"})
  void shouldValidateOsDiskStorageTypeIsValid(String osDiskStorageType) {
    Map<String, String> properties = Collections.singletonMap("os_disk_storage_account_type", osDiskStorageType);

    Map<String, String> errors = new OsDiskStorageTypeValidation().run(properties, null, null);

    assertTrue(errors.isEmpty());
  }

}
