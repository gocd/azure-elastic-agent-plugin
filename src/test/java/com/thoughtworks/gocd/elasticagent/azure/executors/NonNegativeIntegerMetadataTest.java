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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonNegativeIntegerMetadataTest {

  @Test
  void shouldReturnErrorForStrings() {
    NonNegativeIntegerMetadata metadata = new NonNegativeIntegerMetadata("key", true, false);

    Map<String, String> errors = metadata.validate("junk string");

    assertEquals(Collections.singletonMap(metadata.getKey(),
        String.format("%s must be a non negative integer.", metadata.getKey())),
        errors);
  }

  @Test
  void shouldNotReturnErrorIfEmptyAndTheMetadataIsNotRequired() {
    NonNegativeIntegerMetadata metadata = new NonNegativeIntegerMetadata("key", false, false);

    Map<String, String> errors = metadata.validate("");

    assertTrue(errors.isEmpty());
  }

  @Test
  void shouldReturnErrorIfEmptyAndTheMetadataIsRequired() {
    NonNegativeIntegerMetadata metadata = new NonNegativeIntegerMetadata("key", true, false);

    Map<String, String> errors = metadata.validate("");

    assertEquals(Collections.singletonMap(metadata.getKey(),
        String.format("%s must be a non negative integer.", metadata.getKey())),
        errors);
  }

  @Test
  void shouldReturnErrorForNegativeMetadata() {
    NonNegativeIntegerMetadata metadata = new NonNegativeIntegerMetadata("key", true, false);

    Map<String, String> errors = metadata.validate("-20");

    assertEquals(Collections.singletonMap(metadata.getKey(),
        String.format("%s must be a non negative integer.", metadata.getKey())),
        errors);
  }

  @Test
  void shouldNotReturnErrorForNonNegativeMetadata() {
    NonNegativeIntegerMetadata metadata = new NonNegativeIntegerMetadata("key", true, false);

    assertTrue(metadata.validate("0").isEmpty());
    assertTrue(metadata.validate("100").isEmpty());


  }


}
