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
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.executors.GetProfileMetadataExecutor.OS_DISK_SIZE;
import static com.thoughtworks.gocd.elasticagent.azure.executors.GetProfileMetadataExecutor.PLATFORM;

public class OsDiskSizeValidation implements Validation {
  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    String diskSize = properties.get(OS_DISK_SIZE.getKey());
    if (!StringUtils.isBlank(diskSize)) {
      Range<Integer> osDiskSizeRange = getPlatform(properties).osDiskSizeRange();
      if (!osDiskSizeRange.contains(Integer.valueOf(diskSize))) {
        return Collections.singletonMap(OS_DISK_SIZE.getKey(), getRangeMessage(osDiskSizeRange));
      }
    }
    return Collections.emptyMap();
  }

  private Platform getPlatform(Map<String, String> properties) {
    String platform = properties.get(PLATFORM.getKey());
    return StringUtils.isNotBlank(platform)? Platform.valueOf(platform): null;
  }

  private String getRangeMessage(Range<Integer> osDiskSizeRange) {
    return String.format("OS disk size should be between %s and %s.", osDiskSizeRange.getMinimum(), osDiskSizeRange.getMaximum());
  }
}
