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
import com.thoughtworks.gocd.elasticagent.azure.executors.GetClusterProfileMetadataExecutor;
import com.thoughtworks.gocd.elasticagent.azure.models.Field;

import java.util.HashMap;
import java.util.Map;

public class FieldValidation implements Validation {
  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    Map<String, String> errors = new HashMap<>();
    for (Map.Entry<String, Field> entry : GetClusterProfileMetadataExecutor.CLUSTER_PROFILE_FIELDS.entrySet()) {
      Field field = entry.getValue();
      errors.putAll(field.validate(properties.get(entry.getKey())));
    }
    return errors;
  }
}
