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

package com.thoughtworks.gocd.elasticagent.azure.models;

import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import org.junit.jupiter.api.Test;

import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.LINUX;
import static org.junit.jupiter.api.Assertions.*;

class ElasticProfileTest extends BaseTest {

  @Test
  void testShouldFetchImageReferenceFromImageURN() {
    ElasticProfile elasticProfile = new ElasticProfile("",
        "canonical:ubuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX, "Standard_LRS",
        "", "50", "");
    ImageReference imageReference = elasticProfile.getImageReference();

    assertEquals("canonical", imageReference.publisher());
    assertEquals("ubuntuServer", imageReference.offer());
    assertEquals("14.04.4-LTS", imageReference.sku());
    assertEquals("latest", imageReference.version());
  }

  @Test
  void testGetImageReferenceShouldFetchNullWhenImageURNEmpty() {
    assertNull(new ElasticProfile(null, "", "", "", LINUX, "Standard_LRS", "", "50", "").getImageReference());
    assertNull(new ElasticProfile("", "", "", "", LINUX, "Standard_LRS", "", "50", "").getImageReference());
  }

  @Test
  void testShouldFetchOsDiskStorageType() {
    ElasticProfile elasticProfile = new ElasticProfile("",
        "canonical:ubuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX, "Standard_LRS",
        "", "50", "");
    assertEquals(StorageAccountTypes.STANDARD_LRS, elasticProfile.getOsDiskStorageAccountType());
  }

  @Test
  void testShouldDefaultOSDiskStorageTypeWhenNotSet() {
    ElasticProfile elasticProfile = new ElasticProfile("",
        "canonical:ubuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX, "",
        "", "50", "");
    assertEquals(StorageAccountTypes.STANDARD_SSD_LRS, elasticProfile.getOsDiskStorageAccountType());
  }

  @Test
  void shouldSetIdleTimeoutPeriodWhenSet() {
    ElasticProfile elasticProfile = new ElasticProfile("",
        "canonical:ubuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX, "",
        "12", "50", "");

    assertEquals(12, elasticProfile.getIdleTimeoutPeriod().getMinutes());
  }

  @Test
  void shouldSetIdleTimeoutPeriodToNullWhenNotSet() {
    ElasticProfile elasticProfile = new ElasticProfile("",
        "canonical:ubuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX, "",
        "", "50", "");

    assertNull(elasticProfile.getIdleTimeoutPeriod());
  }

  @Test
  void shouldGetOsDiskSize() {
    ElasticProfile elasticProfile = new ElasticProfile("",
        "canonical:ubuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX, "",
        "", "50", "");

    assertEquals(50, elasticProfile.getOsDiskSize().get().intValue());
  }

  @Test
  void shouldGetEmptyOptionalIfDiskSizeNotSet() {
    ElasticProfile elasticProfile = new ElasticProfile("",
        "canonical:ubuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX, "",
        "", "", "");

    assertFalse(elasticProfile.getOsDiskSize().isPresent());
  }
}
