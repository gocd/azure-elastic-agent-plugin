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

import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@EqualsAndHashCode
public class ElasticProfile {

  public static final String VM_IMAGE_URN = "vm_image_urn";
  public static final String VM_SIZE = "vm_size";
  public static final String VM_CUSTOM_IMAGE_ID = "vm_custom_image_id";
  public static final String CUSTOM_SCRIPT = "custom_script";
  public static final String PLATFORM = "platform";
  public static final String OS_DISK_STORAGE_ACCOUNT_TYPE = "os_disk_storage_account_type";
  public static final String IDLE_TIMEOUT = "idle_timeout";
  public static final String OS_DISK_SIZE = "os_disk_size";
  public static final String SUBNET_NAME = "subnet_name";

  @SerializedName(VM_SIZE)
  private String vmSize;

  @SerializedName(VM_IMAGE_URN)
  private String vmImageURN;

  @SerializedName(VM_CUSTOM_IMAGE_ID)
  private String vmCustomImageId;

  @SerializedName(CUSTOM_SCRIPT)
  private String customScript;

  @SerializedName(PLATFORM)
  private Platform platform;

  @SerializedName(OS_DISK_STORAGE_ACCOUNT_TYPE)
  private String osDiskStorageAccountType;

  @SerializedName(IDLE_TIMEOUT)
  @Getter(AccessLevel.NONE)
  private String idleTimeout;

  @SerializedName(OS_DISK_SIZE)
  private String osDiskSize;

  @SerializedName(SUBNET_NAME)
  private String subnetName;

  public ElasticProfile() { }

  public ElasticProfile(String vmSize,
                        String vmImageURN,
                        String customImageId,
                        String customScript,
                        Platform platform,
                        String osDiskStorageAccountType,
                        String idleTimeout, String osDiskSize, String subnet) {
    this.vmSize = vmSize;
    this.vmImageURN = vmImageURN;
    this.vmCustomImageId = customImageId;
    this.customScript = customScript;
    this.platform = platform;
    this.osDiskStorageAccountType = osDiskStorageAccountType;
    this.idleTimeout = idleTimeout;
    this.osDiskSize = osDiskSize;
    this.subnetName = subnet;
  }

  public ImageReference getImageReference() {
    return isBlank(vmImageURN) ? null : new ImageURN(vmImageURN).toImageReference();
  }

  public String hash() {
    return String.valueOf(hashCode());
  }

  public StorageAccountTypes getOsDiskStorageAccountType() {
    return isBlank(osDiskStorageAccountType) ? StorageAccountTypes.STANDARD_SSD_LRS : StorageAccountTypes.fromString(osDiskStorageAccountType);
  }

  public Period getIdleTimeoutPeriod(){
    if (StringUtils.isNotBlank(this.idleTimeout)) {
      return new Period().withMinutes(Integer.parseInt(this.idleTimeout));
    }
    return null;
  }

  public Optional<Integer> getOsDiskSize() {
    return isBlank(osDiskSize) ? Optional.empty() : Optional.of(Integer.parseInt(osDiskSize));
  }

  public Optional<String> getSubnetName() {
    return isBlank(subnetName) ? Optional.empty() : Optional.of(subnetName);
  }
}
