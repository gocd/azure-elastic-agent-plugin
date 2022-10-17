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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class NonNegativeIntegerMetadata extends Metadata{

  public NonNegativeIntegerMetadata(String key, boolean required, boolean secure) {
    super(key, new ProfileMetadata(required, secure));
  }

  public NonNegativeIntegerMetadata(String key) {
    super(key, new ProfileMetadata(false, false));
  }

  public Map<String, String> validate(String input) {
    HashMap<String, String> result = new HashMap<>();
    String validationError = doValidate(input);
    if (isNotBlank(validationError)) {
      result.put(getKey(), validationError);
    }
    return result;
  }

  protected String doValidate(String input) {
    try {
      if (Integer.parseInt(input) < 0) {
        return getKey() + " must be a non negative integer.";
      }
    } catch (NumberFormatException e) {
      if(!isRequired() && StringUtils.isBlank(input)){
        return null;
      }
      return getKey() + " must be a non negative integer.";
    }

    return null;
  }



}
