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

import com.thoughtworks.gocd.elasticagent.azure.Errors;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;

import java.util.Collections;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import static com.thoughtworks.gocd.elasticagent.azure.Errors.AZURE_INVALID_NSG_ID;
import static com.thoughtworks.gocd.elasticagent.azure.executors.GetPluginConfigurationExecutor.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AzureNetworkSettingsValidation implements Validation {

  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    String networkId = properties.get(NETWORK_ID.key());
    if (isNotBlank(networkId) && !client.networkExists(networkId)) {
      LOG.debug("Network Id provided {} is invalid", networkId);
      return Collections.singletonMap(NETWORK_ID.key(), Errors.AZURE_INVALID_NETWORK_ID);
    }

    String subnetNamesString = properties.get(SUBNET_NAMES.key());
    if (isNotBlank(subnetNamesString)) {
      String[] subnetNames = Util.splitByComma(subnetNamesString);
      for (String subnetName : subnetNames) {
        if (!client.subnetExists(networkId, subnetName)) {
          LOG.debug("Subnet provided {} is does not exist in the given Network {}", subnetName, networkId);
          return Collections.singletonMap(SUBNET_NAMES.key(), String.format(Errors.AZURE_INVALID_SUBNET_MESSAGE_FORMAT, subnetName));
        }
      }
    }

    String nsgId = properties.get(NETWORK_SECURITY_GROUP_ID.key());
    if (isNotBlank(nsgId) && !client.networkSecurityGroupExists(nsgId)) {
      LOG.debug("Network Security group provided {} is invalid", nsgId);
      return Collections.singletonMap(NETWORK_SECURITY_GROUP_ID.key(), AZURE_INVALID_NSG_ID);
    }

    return Collections.emptyMap();
  }
}
