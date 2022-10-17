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

class LinuxUsernameFieldTest {

  private LinuxUsernameField field;

  @BeforeEach
  void setUp() {
    field = new LinuxUsernameField("", "Name", "", true, false, "1");
  }

  @Test
  void testValidateUsernameLengthBetween1And32() {
    assertEquals("Name must not be blank.", field.doValidate(""));
    assertEquals("Name must not be blank.", field.doValidate(null));
    assertEquals(field.getPatternMismatchError(), field.doValidate("abcdefghijklmnopqrstuvwxyz1234567"));
    assertNull(field.doValidate("abcdefghijklmnopqrstuvwxyz123456"));
    assertNull(field.doValidate("abcdefgh56"));
  }

  @Test
  void testValidateUsernameCannotHaveSpacesOrNewLines() {
    assertEquals(field.getPatternMismatchError(), field.doValidate("abcd efr"));
    assertEquals(field.getPatternMismatchError(), field.doValidate("foo\nbar"));
    assertEquals(field.getPatternMismatchError(), field.doValidate("  foobar"));
    assertEquals(field.getPatternMismatchError(), field.doValidate("  foo  bar \n"));
  }

  @Test
  void testValidateUsernameCanContainUnderScoresAndHyphens() {
    assertNull(field.doValidate("abCD122_efr"));
    assertNull(field.doValidate("foo-b45ar"));
  }

  @Test
  void testValidateUsernameMustNotStartWithHyphenOrNumber() {
    assertEquals(field.getPatternMismatchError(), field.doValidate("-abcd122_efr"));
    assertEquals(field.getPatternMismatchError(), field.doValidate("12foo-b45ar"));
    assertNull(field.doValidate("_12foo-b45ar"));
  }

  @Test
  void testValidateUsernameMustNotContainSpecialCharacters() {
    assertEquals(field.getPatternMismatchError(), field.doValidate("ansj%^&3()"));
    assertEquals(field.getPatternMismatchError(), field.doValidate("use学中文r"));
  }
}
