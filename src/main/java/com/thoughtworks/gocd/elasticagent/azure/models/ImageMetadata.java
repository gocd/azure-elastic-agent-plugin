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

package com.thoughtworks.gocd.elasticagent.azure.models;


import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;

import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.validations.ImageValidation.IMAGE_URN_OR_CUSTOM_IMAGE_SET_MESSAGE;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ImageMetadata {

  public static final String INVALID_IMAGE_URN_PATTERN_MESSAGE = "Image URN must be in the correct format: Publisher:Offer:Sku:Version";
  public static final String INVALID_IMAGE_FOR_PLATFORM_OR_REGION_MESSAGE = "Provided Image URN is not valid for the platform and region selected.";
  public static final String IMAGE_URN_INVALID_GENERIC_MESSAGE = "Image URN is invalid";
  private final String imageURNKey;
  private final String customImageKey;

  public ImageMetadata(String imageURNKey, String customImageKey) {
    this.imageURNKey = imageURNKey;
    this.customImageKey = customImageKey;
  }

  public HashMap<String, String> validate(String imageURN,
                                          String customImageId,
                                          Region region,
                                          Platform platform,
                                          GoCDAzureClient goCDAzureClient) {
    HashMap<String, String> errors = new HashMap<>();
    if (isBlank(customImageId) && isBlank(imageURN)) {
      errors.put(customImageKey, IMAGE_URN_OR_CUSTOM_IMAGE_SET_MESSAGE);
      errors.put(imageURNKey, IMAGE_URN_OR_CUSTOM_IMAGE_SET_MESSAGE);
      return errors;
    }
    if (!isBlank(imageURN)) {
      return validateImageUrn(imageURN, region, goCDAzureClient, platform);
    }
    return errors;
  }

  private HashMap<String, String> validateImageUrn(String imageURN, Region region, GoCDAzureClient goCDAzureClient, Platform platform) {
    HashMap<String, String> errors = new HashMap<>();
    ImageURN urn = imageURN(imageURN);
    if (urn == null) {
      errors.put(imageURNKey, INVALID_IMAGE_URN_PATTERN_MESSAGE);
      return errors;
    }
    validateImageUrnForPlatform(goCDAzureClient, urn, platform, region, errors);
    return errors;
  }

  private void validateImageUrnForPlatform(GoCDAzureClient goCDAzureClient, ImageURN urn, Platform platform, Region region, Map<String, String> errors) {
    try {
      if (!goCDAzureClient.imageValidForPlatform(urn, platform, region)) {
        errors.put(imageURNKey, INVALID_IMAGE_FOR_PLATFORM_OR_REGION_MESSAGE);
      }
    } catch (Exception e) {
      errors.put(imageURNKey, IMAGE_URN_INVALID_GENERIC_MESSAGE);
    }
  }

  private ImageURN imageURN(String input) {
    try {
      return new ImageURN(input);
    } catch (RuntimeException e) {
      return null;
    }
  }
}
