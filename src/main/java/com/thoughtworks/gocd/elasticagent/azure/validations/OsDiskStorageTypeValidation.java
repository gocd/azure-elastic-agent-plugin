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

import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.thoughtworks.gocd.elasticagent.azure.executors.GetProfileMetadataExecutor.OS_DISK_STORAGE_ACCOUNT_TYPE;

public class OsDiskStorageTypeValidation implements Validation {
  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    String osDiskType = properties.get(OS_DISK_STORAGE_ACCOUNT_TYPE.getKey());
    List<String> storageAccountTypeNames = StorageAccountTypes.values().stream().map(StorageAccountTypes::toString).collect(Collectors.toList());
    if (storageAccountTypeNames.stream().noneMatch(storageAccountTypes -> storageAccountTypes.equals(osDiskType))) {
      String supportedOsTypes = storageAccountTypeNames.stream().collect(Collectors.joining(", "));
      return Collections.singletonMap(OS_DISK_STORAGE_ACCOUNT_TYPE.getKey(), String.format("Supported OS disk storage types: %s.", supportedOsTypes));
    }
    return Collections.emptyMap();
  }
}
