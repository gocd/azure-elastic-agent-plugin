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

package com.thoughtworks.gocd.elasticagent.azure.requests;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.executors.ValidateConfigurationExecutor;
import com.thoughtworks.gocd.elasticagent.azure.validations.Validation;

import java.util.HashMap;
import java.util.Map;

public class ValidatePluginSettings extends HashMap<String, String> {
  public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

  public static ValidatePluginSettings fromJSON(String json) {
    ValidatePluginSettings result = new ValidatePluginSettings();

    Map<String, Map<String, Map<String, String>>> allSettings = GSON.fromJson(json, new TypeToken<Map<String, Map<String, Map<String, String>>>>() {
    }.getType());

    Map<String, Map<String, String>> settings = allSettings.get("plugin-settings");

    for (Map.Entry<String, Map<String, String>> entry : settings.entrySet()) {
      result.put(entry.getKey(), entry.getValue().get("value"));
    }

    return result;
  }

  public RequestExecutor executor(GoCDAzureClientFactory goCDAzureClientFactory) {
    return new ValidateConfigurationExecutor(this, goCDAzureClientFactory, Validation.PLUGIN_SETTINGS_VALIDATIONS);
  }
}
