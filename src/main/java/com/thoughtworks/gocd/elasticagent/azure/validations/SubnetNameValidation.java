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
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class SubnetNameValidation implements Validation {
  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    HashMap<String, String> errors = new HashMap<>();
    String subnetName = properties.get(ElasticProfile.SUBNET_NAME);
    if (!isBlank(subnetName) && !ArrayUtils.contains(settings.getSubnetNames(), subnetName)) {
      errors.put(ElasticProfile.SUBNET_NAME, String.format("Subnet name must be one of: %s", StringUtils.join(settings.getSubnetNames(), ", ")));
    }
    return errors;
  }
}
