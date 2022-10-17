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

class WindowsPasswordFieldTest {

  private WindowsPasswordField field;

  @BeforeEach
  void setUp() {
    field = new WindowsPasswordField("", "password", "", true, "1");
  }

  @Test
  void shouldValidateThePasswordLengthShouldBeAtleast12CharactersLong() {
    assertEquals(field.getPatternMismatchError(), field.doValidate("abc123456@"));
    System.out.println(field.doValidate("abc123456@"));
  }

  @Test
  void shouldValidateIfThePasswordHasAtleast1LowercaseAndSpecialCharacter() {
    assertEquals(field.getPatternMismatchError(), field.doValidate("ABCDEFGH1234"));
  }

  @Test
  void shouldValidateIfThePasswordHasAtleast1UppercaseAndSpecialCharacter() {
    assertEquals(field.getPatternMismatchError(), field.doValidate("abcdefgh1234"));
  }

  @Test
  void shouldValidateIfThePasswordHasAtleast1LowercaseSpecialCharacterAndDigit() {
    assertNull(field.doValidate("abc1234567@8"));

  }

  @Test
  void shouldValidateIfThePasswordHasAtleast1UppercaseSpecialCharacterAndDigit() {
    assertNull(field.doValidate("ABC1234567@8"));

  }

  @Test
  void shouldValidateIfThePasswordHasAtleast1Uppercase1LowercaseAndSpecialCharacter() {
    assertNull(field.doValidate("ABCDabcd@#$%"));
  }

  @Test
  void shouldValidateIfThePasswordHasAtleast1Uppercase1LowercaseAndDigit() {
    assertNull(field.doValidate("ABCDabcd1234"));
  }

}
