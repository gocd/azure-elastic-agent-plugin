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

package com.thoughtworks.gocd.elasticagent.azure.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NonNegativeNumberFieldTest {

  private NonNegativeNumberField field = new NonNegativeNumberField("key", "field", "0",
      false, false, "1");

  @Test
  void shouldValidateIfNegativeNumberIsPassed() {
    assertEquals("field must be a non negative integer.", field.doValidate("-2"));
  }

  @Test
  void shouldValidateIfZeroIsPassed() {
    assertNull(field.doValidate("0"));
  }

  @Test
  void shouldValidateIfPositiveNumberIsPassed() {
    assertNull(field.doValidate("10"));
  }

  @Test
  void shouldValidateIfNonIntegerIsPassed() {
    assertEquals("field must be a non negative integer.", field.doValidate("junk string"));
  }

  @Test
  void shouldValidateIfEmptyStringIsPassedAndItIsANotARequiredField() {
    assertNull(field.doValidate(""));
  }

  @Test
  void shouldValidateIfEmptyStringIsPassedAndItIsAARequiredField() {
    field = new NonNegativeNumberField("key", "field", "0",
        true, false, "1");
    assertEquals("field must be a non negative integer.", field.doValidate(""));
  }

}
