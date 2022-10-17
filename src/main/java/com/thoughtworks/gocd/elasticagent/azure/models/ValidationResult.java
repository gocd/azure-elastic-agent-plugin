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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationResult {

  private final Map<String, String> errors = new HashMap<>();
  private static final Gson GSON = new Gson();

  public void addError(String key, String message) {
    this.errors.put(key, message);
  }

  public String toJson() {
    return GSON.toJson(toErrorsArray());
  }

  private List<Map<String, String>> toErrorsArray() {
    return errors.entrySet().stream().map(errorEntry -> {
      Map<String, String> errorMap = new HashMap<String, String>();
      errorMap.put("key", errorEntry.getKey());
      errorMap.put("message", errorEntry.getValue());
      return errorMap;
    }).collect(Collectors.toCollection(ArrayList::new));
  }

  public void addErrors(Map<String, String> validationErrors) {
    errors.putAll(validationErrors);
  }
}
