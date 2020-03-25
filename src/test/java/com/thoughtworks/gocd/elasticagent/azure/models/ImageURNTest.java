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

import com.microsoft.azure.management.compute.ImageReference;
import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageURNTest extends BaseTest {

  @Test
  void testShouldCreateImageReferenceWithDefaultVersion() {
    ImageReference imageReference = new ImageURN("Publisher:offer:sku").toImageReference();

    assertEquals("Publisher", imageReference.publisher());
    assertEquals("offer", imageReference.offer());
    assertEquals("sku", imageReference.sku());
    assertEquals("latest", imageReference.version());
  }

  @Test
  void testShouldCreateImageReferenceWithVersion() {
    ImageReference imageReference = new ImageURN("Publisher:Offer:Sku:12.3").toImageReference();

    assertEquals("Publisher", imageReference.publisher());
    assertEquals("Offer", imageReference.offer());
    assertEquals("Sku", imageReference.sku());
    assertEquals("12.3", imageReference.version());
  }

  @Test
  void testShouldThrowRuntimeExceptionOnInvalidUrn() {
    assertThrows(RuntimeException.class, () -> new ImageURN("invalid:format"));
  }

  @Test
  void shouldBuildImageURNFromImageReference() {
    ImageReference imageRef = new ImageReference().withPublisher("MS")
        .withOffer("offer")
        .withSku("sku")
        .withVersion("v10");
    ImageURN urn = new ImageURN(imageRef);

    assertEquals("MS", urn.getPublisher());
    assertEquals("offer", urn.getOffer());
    assertEquals("sku", urn.getSku());
    assertEquals("v10", urn.getVersion());
    assertEquals("MS:offer:sku:v10", urn.toString());
  }
}
