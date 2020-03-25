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
import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.WINDOWS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ImageMetadataTest extends BaseTest {

  private ImageMetadata imageMetadata;

  @Mock
  GoCDAzureClient mockGoCDAzureClient;

  @BeforeEach
  void setUp() {
    initMocks(this);
    imageMetadata = new ImageMetadata("vm_image_urn", "vm_custom_image_id");
  }

  @Test
  void testShouldBeInvalidIfBothImagePropertiesBlank() {
    Map<String, String> errors = imageMetadata.validate("", "", null, null, mockGoCDAzureClient);

    assertEquals(2, errors.size());
    assertEquals("Image URN or Custom image id must be set.", errors.get("vm_image_urn"));
    assertEquals("Image URN or Custom image id must be set.", errors.get("vm_custom_image_id"));
  }

  @Test
  void testShouldReturnErrorForInvalidateImageUrn() {
    Map<String, String> errors = imageMetadata.validate("invalid", "", null, null, mockGoCDAzureClient);

    assertEquals(1, errors.size());
    assertEquals("Image URN must be in the correct format: Publisher:Offer:Sku:Version", errors.get("vm_image_urn"));
  }

  @Test
  void shouldValidateImageURNAgainstRegion() {
    ImageURN urn = new ImageURN("publisher:offer:sku:version");
    when(mockGoCDAzureClient.imageValidForPlatform(urn, WINDOWS, Region.US_WEST)).thenReturn(false);

    HashMap<String, String> errors = imageMetadata.validate("publisher:offer:sku:version",
        "",
        Region.US_WEST,
        WINDOWS,
        mockGoCDAzureClient);
    assertEquals(1, errors.size());
    assertEquals("Provided Image URN is not valid for the platform and region selected.", errors.get("vm_image_urn"));

  }


  @Test
  void testShouldBeValidIfBothImageUrnAndCustomIdSet() {
    ImageURN urn = new ImageURN("publisher:offer:sku:version");
    when(mockGoCDAzureClient.imageValidForPlatform(eq(urn), eq(WINDOWS), eq(Region.INDIA_SOUTH))).thenReturn(true);

    Map<String, String> errors = imageMetadata.validate("publisher:offer:sku:version",
        "id",
        Region.INDIA_SOUTH,
        WINDOWS,
        mockGoCDAzureClient);

    assertEquals(0, errors.size());
  }
}
