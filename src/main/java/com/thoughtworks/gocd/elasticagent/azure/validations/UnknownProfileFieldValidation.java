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

package com.thoughtworks.gocd.elasticagent.azure.validations;

import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.executors.GetProfileMetadataExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class UnknownProfileFieldValidation implements Validation {
  public static final String UNKNOWN_PROPERTY_MESSAGE = "Is an unknown property";

  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    HashMap<String, String> errors = new HashMap<>();
    Set<String> set = new HashSet<>(properties.keySet());
    set.removeAll(GetProfileMetadataExecutor.FIELDS.stream().map(metadata -> metadata.getKey()).collect(Collectors.toCollection(ArrayList::new)));
    set.forEach(s -> errors.put(s, UNKNOWN_PROPERTY_MESSAGE));
    return errors;
  }
}
