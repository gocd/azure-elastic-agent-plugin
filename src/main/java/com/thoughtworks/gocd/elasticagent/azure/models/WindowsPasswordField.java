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

public class WindowsPasswordField extends PatternMatchingField {

  public static final String INVALID_PATTERN_ERROR_MESSAGE_FORMAT = "%s should be 12-123 characters long and meet atleast 3 of the following criteria\n" +
      "1.Have lower characters\n" +
      "2.Have upper characters\n" +
      "3.Have a digit\n" +
      "4.Have a special character";

  public static final String REGEX = "(?=^.{12,123}$)((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])|(?=.*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))^.*";

  public WindowsPasswordField(String key, String displayName, String defaultValue, Boolean required, String displayOrder) {
    super(key, displayName, defaultValue, required, true, displayOrder);
  }

  @Override
  public String getPattern() {
    return REGEX;
  }

  @Override
  public String getPatternMismatchError() {
    return String.format(INVALID_PATTERN_ERROR_MESSAGE_FORMAT, this.displayName);
  }
}
