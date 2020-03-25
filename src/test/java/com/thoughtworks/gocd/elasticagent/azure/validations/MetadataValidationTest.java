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

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetadataValidationTest {

  @Test
  void shouldValidateMandatoryKeys() {
    Map<String, String> errors = new MetadataValidation().run(Collections.emptyMap(), null, null);
    assertEquals(3, errors.size());
    assertEquals("vm_size must not be blank.", errors.get("vm_size"));
    assertEquals("os_disk_storage_account_type must not be blank.", errors.get("os_disk_storage_account_type"));
    assertEquals("platform must not be blank.", errors.get("platform"));
  }

}
