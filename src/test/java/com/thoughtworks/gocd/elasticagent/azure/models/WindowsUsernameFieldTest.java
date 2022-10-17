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

package com.thoughtworks.gocd.elasticagent.azure.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WindowsUsernameFieldTest {

  private WindowsUsernameField field;

  @BeforeEach
  void setUp() {
    field = new WindowsUsernameField("", "Name", "", true, "1");
  }

  @Test
  void shouldValidateWindowsUserNameLengthBetween1And20() {
    assertEquals("Name must not be blank.", field.doValidate(""));
    assertEquals("Name must not be blank.", field.doValidate(null));
    assertNull(field.doValidate("a"));
    assertEquals(field.getPatternMismatchError(), field.doValidate("some_unique_username_1234"));
  }

  @Test
  void shouldValidateWindowsUserNameShouldNotEndWithAPeriod() {
    assertEquals(field.getPatternMismatchError(), field.doValidate("invalid_username."));
  }

  @Test
  void shouldValidateWindowsUserNameCanContainAPeriodInBetween() {
    assertNull(field.doValidate("valid.username"));
  }

}
