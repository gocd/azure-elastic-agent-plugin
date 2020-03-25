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

package com.thoughtworks.gocd.elasticagent.azure;

import com.microsoft.azure.management.compute.*;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.microsoft.azure.management.compute.VirtualMachineSizeTypes.STANDARD_D3_V2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AzureInstanceMapperTest {

  @Test
  void shouldMapVirtualMachineToAzureInstanceModel() {
    VirtualMachine mockVM = mock(VirtualMachine.class, RETURNS_DEEP_STUBS);
    when(mockVM.name()).thenReturn("VM-elastic-agent-id-123");
    when(mockVM.id()).thenReturn("resource-id");

    Map<String, String> tags = Collections.singletonMap("key", "value");
    when(mockVM.tags()).thenReturn(tags);
    DateTime provisionTime = new DateTime(2018, 11, 3, 5, 56);
    List<InstanceViewStatus> statuses = Arrays.asList(new InstanceViewStatus().withCode("PowerState/running").withTime(new DateTime()),
        new InstanceViewStatus().withCode("ProvisioningState/succeeded").withTime(provisionTime));

    when(mockVM.instanceView().statuses()).thenReturn(statuses);
    when(mockVM.provisioningState()).thenReturn("succeeded");
    when(mockVM.powerState()).thenReturn(PowerState.RUNNING);
    ImageReference imageReference = new ImageReference();
    when(mockVM.storageProfile().imageReference()).thenReturn(imageReference);
    when(mockVM.instanceView().computerName()).thenReturn("vm-host-id-1234");
    when(mockVM.inner().hardwareProfile().vmSize()).thenReturn(STANDARD_D3_V2);
    when(mockVM.instanceView().osName()).thenReturn("Windows Server");
    when(mockVM.osDiskSize()).thenReturn(2);
    when(mockVM.resourceGroupName()).thenReturn("resource-group");
    when(mockVM.getPrimaryNetworkInterface().name()).thenReturn("nic-123");
    when(mockVM.osType()).thenReturn(OperatingSystemTypes.WINDOWS);

    AzureInstance instance = new AzureInstanceMapper().map(mockVM);

    assertEquals("VM-elastic-agent-id-123", instance.getName());
    assertEquals("resource-id", instance.getId());
    assertEquals(provisionTime.toDateTime(DateTimeZone.UTC), instance.getCreatedAt());
    assertEquals("succeeded", instance.getProvisioningState());
    assertEquals("PowerState/running", instance.getPowerState());
    assertEquals(imageReference, instance.getImageReference());
    assertEquals("vm-host-id-1234", instance.getHostName());
    assertEquals(STANDARD_D3_V2.toString(), instance.getSize());
    assertEquals("Windows Server", instance.getOs());
    assertEquals(Integer.valueOf(2), instance.getDiskSize());
    assertEquals(tags, instance.getTags());
    assertEquals("resource-group", instance.getResourceGroupName());
    assertEquals("nic-123", instance.getPrimaryNetworkInterface());
    assertEquals(Platform.WINDOWS, instance.getPlatform());

  }

}
