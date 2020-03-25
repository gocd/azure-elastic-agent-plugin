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

package com.thoughtworks.gocd.elasticagent.azure.validations;

import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface Validation {

  List<Validation> ELASTIC_PROFILE_VALIDATIONS = Arrays.asList(new OsDiskStorageTypeValidation(),
      new MetadataValidation(),
      new OsDiskSizeValidation(),
      new SubnetNameValidation(),
      new UnknownProfileFieldValidation(),
      new ImageValidation());

  List<Validation> PLUGIN_SETTINGS_VALIDATIONS = Arrays.asList(new FieldValidation(),
      new AzureNetworkSettingsValidation(),
      new AzureResourceGroupValidation(),
      new AzureRegionValidation());

  Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client);

  default Map<String, String> run(Map<String, String> properties, GoCDAzureClient client) {
    return run(properties, null, client);
  }
}
