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
import com.thoughtworks.gocd.elasticagent.azure.models.ImageMetadata;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.executors.GetProfileMetadataExecutor.*;

public class ImageValidation implements Validation {
  public static final String IMAGE_URN_OR_CUSTOM_IMAGE_SET_MESSAGE = "Image URN or Custom image id must be set.";

  @Override
  public Map<String, String> run(Map<String, String> properties, PluginSettings settings, GoCDAzureClient client) {
    ImageMetadata imageMetadata = new ImageMetadata(VM_IMAGE_URN.getKey(),
        VM_CUSTOM_IMAGE_ID.getKey());
    return imageMetadata.validate(
        properties.get(VM_IMAGE_URN.getKey()),
        properties.get(VM_CUSTOM_IMAGE_ID.getKey()),
        settings.getRegion(),
        getPlatform(properties),
        client);
  }

  private Platform getPlatform(Map<String, String> properties) {
    String platform = properties.get(PLATFORM.getKey());
    return StringUtils.isNotBlank(platform) ? Platform.valueOf(platform) : null;
  }
}
