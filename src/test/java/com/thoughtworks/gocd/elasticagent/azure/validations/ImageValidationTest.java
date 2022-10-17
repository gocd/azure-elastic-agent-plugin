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

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.models.ImageURN;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.executors.GetProfileMetadataExecutor.VM_IMAGE_URN;
import static com.thoughtworks.gocd.elasticagent.azure.models.ImageMetadata.IMAGE_URN_INVALID_GENERIC_MESSAGE;
import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.LINUX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class ImageValidationTest {

  @Mock
  private PluginSettings mockSettings;

  @Mock
  private GoCDAzureClient mockClient;

  @BeforeEach
  void setUp() {
    openMocks(this);
    when(mockSettings.getRegion()).thenReturn(Region.US_WEST);
  }

  @Test
  void shouldValidateImageUrnOrCustomImageIdIsSet() {
    Map<String, String> errors = new ImageValidation().run(Collections.emptyMap(), mockSettings, mockClient);

    assertEquals(2, errors.size());
    assertEquals("Image URN or Custom image id must be set.", errors.get("vm_image_urn"));
    assertEquals("Image URN or Custom image id must be set.", errors.get("vm_custom_image_id"));
  }

  @Test
  void shouldBeValidIfImageIdSetButImageUrnNotSet() {
    Map<String, String> properties = Collections.singletonMap("vm_custom_image_id", "custom_image_id");

    Map<String, String> errors = new ImageValidation().run(properties, mockSettings, mockClient);

    assertTrue(errors.isEmpty());
  }

  @Test
  void shouldValidImageUrnPattern() {
    Map<String, String> properties = Collections.singletonMap("vm_image_urn", "invalid_pattern");

    Map<String, String> errors = new ImageValidation().run(properties, mockSettings, mockClient);

    assertEquals(1, errors.size());
    assertEquals("Image URN must be in the correct format: Publisher:Offer:Sku:Version", errors.get("vm_image_urn"));
  }

  @Test
  void shouldValidateImageURNAgainstRegionAndPlatformProvided() {
    ImageURN urn = new ImageURN("pub:off:sku:latest");
    when(mockClient.imageValidForPlatform(urn, LINUX, Region.US_WEST)).thenReturn(true);
    HashMap<String, String> properties = new HashMap<>();
    properties.put("vm_image_urn", "pub:off:sku:latest");
    properties.put("platform", "LINUX");

    Map<String, String> errors = new ImageValidation().run(properties, mockSettings, mockClient);

    assertTrue(errors.isEmpty());
    verify(mockClient).imageValidForPlatform(urn, LINUX, Region.US_WEST);
  }

  @Test
  void shouldValidateImageURNWhenCannotFetchImageDataFromAzure() {
    ImageURN urn = new ImageURN("pub:off:sku:latest");
    when(mockClient.imageValidForPlatform(urn, LINUX, Region.US_WEST)).thenThrow(CloudException.class);
    HashMap<String, String> properties = new HashMap<>();
    properties.put("vm_image_urn", "pub:off:sku:latest");
    properties.put("platform", "LINUX");

    Map<String, String> errors = new ImageValidation().run(properties, mockSettings, mockClient);

    assertEquals(1, errors.size());
    assertEquals(IMAGE_URN_INVALID_GENERIC_MESSAGE, errors.get(VM_IMAGE_URN.getKey()));
    verify(mockClient).imageValidForPlatform(urn, LINUX, Region.US_WEST);
  }
}
