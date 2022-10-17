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

import static java.lang.String.format;

public class LinuxUsernameField extends PatternMatchingField{

  private static final String INVALID_NAME_ERROR_MESSAGE_FORMAT = "%s should be 1-32 characters long with alphanumeric characters, underscores or hyphens. Cannot start with a number or hyphen.";
  private static final String NAME_REGEX = "^[^-0-9][a-zA-Z0-9_-]{1,31}$";

  public LinuxUsernameField(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
    super(key, displayName, defaultValue, required, false, displayOrder);
  }

  @Override
  public String getPattern() {
    return NAME_REGEX;
  }

  @Override
  public String getPatternMismatchError() {
    return format(INVALID_NAME_ERROR_MESSAGE_FORMAT, this.displayName);
  }
}
