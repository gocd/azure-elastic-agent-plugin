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

import com.thoughtworks.gocd.elasticagent.azure.Errors;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;

import java.util.Collections;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import static com.thoughtworks.gocd.elasticagent.azure.executors.GetPluginConfigurationExecutor.RESOURCE_GROUP;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AzureResourceGroupValidation implements Validation {

  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    String resourceGroup = properties.get(RESOURCE_GROUP.key());
    if (isNotBlank(resourceGroup) && !client.resourceGroupExists(resourceGroup)) {
      LOG.debug("Provided resource group {} does not exist", resourceGroup);
      return Collections.singletonMap(RESOURCE_GROUP.key(), Errors.AZURE_INVALID_RESOURCE_GROUP);
    }
    return Collections.emptyMap();
  }
}
