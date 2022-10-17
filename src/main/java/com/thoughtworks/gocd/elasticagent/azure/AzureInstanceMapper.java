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

package com.thoughtworks.gocd.elasticagent.azure;

import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Map;

public class AzureInstanceMapper {

  public static final String PROVISIONING_STATE_SUCCEEDED = "ProvisioningState/succeeded";

  public AzureInstance map(VirtualMachine virtualMachine){

    String name = virtualMachine.name();
    String id = virtualMachine.id();
    String provisioningState = virtualMachine.provisioningState();
    String powerState = virtualMachine.powerState().toString();
    int diskSize = virtualMachine.osDiskSize();
    DateTime createdAt = getProvisionTime(virtualMachine);
    ImageReference imageReference = getImageReference(virtualMachine);
    String hostName = getHostName(virtualMachine);
    String size = getSize(virtualMachine);
    String os = getOs(virtualMachine);
    Platform platform = getPlatform(virtualMachine);
    Map<String, String> tags = virtualMachine.tags();
    String resourceGroupName = virtualMachine.resourceGroupName();
    String primaryNetworkInterface = virtualMachine.getPrimaryNetworkInterface().name();

    return new AzureInstance(name,
        hostName,
        id,
        createdAt,
        imageReference,
        size,
        os,
        diskSize,
        provisioningState,
        powerState,
        resourceGroupName,
        primaryNetworkInterface,
        tags,
        platform);
  }

  private Platform getPlatform(VirtualMachine virtualMachine) {
    return virtualMachine.osType().equals(OperatingSystemTypes.WINDOWS) ? Platform.WINDOWS : Platform.LINUX;
  }

  private String getOs(VirtualMachine virtualMachine) {
    return virtualMachine.instanceView().osName();
  }

  private String getHostName(VirtualMachine virtualMachine) {
    return virtualMachine.instanceView().computerName();
  }

  private String getSize(VirtualMachine virtualMachine) {
    return virtualMachine.inner().hardwareProfile().vmSize().toString();
  }

  private ImageReference getImageReference(VirtualMachine virtualMachine) {
    return virtualMachine.storageProfile().imageReference();
  }

  private DateTime getProvisionTime(VirtualMachine virtualMachine) {
    return virtualMachine.instanceView().statuses().stream()
        .filter((instanceViewStatus -> instanceViewStatus.code().equals(PROVISIONING_STATE_SUCCEEDED)))
        .findFirst().map(InstanceViewStatus::time)
        .orElse(DateTime.now())
        .toDateTime(DateTimeZone.UTC);
  }
}
