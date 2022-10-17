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

public class WindowsUsernameField extends PatternMatchingField {

  public static final String INVALID_NAME_ERROR_MESSAGE_FORMAT = "%s should be 1-20 characters long with lowercase,numeric characters, underscores. Cannot end with a period";
  public static final String REGEX = "^[a-z0-9_\\.]{0,20}[^\\.]$";

  public WindowsUsernameField(String key, String displayName, String defaultValue, Boolean required, String displayOrder) {
    super(key, displayName, defaultValue, required, false, displayOrder);
  }

  @Override
  public String getPattern() {
    return REGEX;
  }

  @Override
  public String getPatternMismatchError() {
    return String.format(INVALID_NAME_ERROR_MESSAGE_FORMAT, this.displayName);
  }
}
