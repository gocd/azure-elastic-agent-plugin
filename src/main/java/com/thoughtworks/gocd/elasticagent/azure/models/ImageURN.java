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
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ImageURN {
  public static final String DEFAULT_VERSION = "latest";
  public static final String DELIMITER = ":";
  private final String publisher;
  private final String offer;
  private final String sku;
  private final String version;

  public ImageURN(String urnString) throws InvalidImageURNException {
    String[] split = urnString.split(DELIMITER);
    if (split.length < 3) {
      throw new InvalidImageURNException(String.format("Invalid image URN format: %s", urnString));
    }
    this.publisher = split[0];
    this.offer = split[1];
    this.sku = split[2];
    this.version = split.length > 3 ? split[3] : DEFAULT_VERSION;
  }

  public ImageURN(ImageReference imageReference){
    this.publisher = imageReference.publisher();
    this.offer = imageReference.offer();
    this.sku = imageReference.sku();
    this.version = imageReference.version();
  }

  public ImageReference toImageReference() {
    return new ImageReference().withPublisher(publisher).withOffer(offer).withSku(sku).withVersion(version);
  }

  public class InvalidImageURNException extends RuntimeException {
    public InvalidImageURNException(String message) {
      super(message);
    }
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s:%s", publisher, offer, sku, version);
  }
}
