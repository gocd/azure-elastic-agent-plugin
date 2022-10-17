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

import org.apache.commons.lang3.StringUtils;

public class NonNegativeNumberField extends Field {
  public NonNegativeNumberField(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
    super(key, displayName, defaultValue, required, secure, displayOrder);
  }

  @Override
  public String doValidate(String input) {
    try {
      if (Integer.parseInt(input) < 0) {
        return this.displayName + " must be a non negative integer.";
      }
    } catch (NumberFormatException e) {
      if(!this.required && StringUtils.isBlank(input)){
        return null;
      }
      return this.displayName + " must be a non negative integer.";
    }

    return null;
  }
}
