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

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import com.thoughtworks.gocd.elasticagent.azure.vm.VmConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.LINUX;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.GOCD_SERVER_ID_TAG_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AzureInstanceManagerTest extends BaseTest {

  @Mock
  GoCDAzureClient mockGoCDAzureClient;

  @Mock
  private AzureInstanceMapper mapper;

  private AzureInstanceManager azureInstanceManager;

  @BeforeEach
  void setup() {
    openMocks(this);
    azureInstanceManager = new AzureInstanceManager(mapper);
  }

  @Test
  void shouldCreateAzureInstanceAndStoreExecutionLogs() throws Exception {
    ArgumentCaptor<VmConfig> vmConfigCaptor = ArgumentCaptor.forClass(VmConfig.class);
    ElasticProfile elasticProfile = new ElasticProfile("Standard_D3_v2",
        "Canonical:UbuntuServer:16.04.0-LTS",
        "",
        "",
        LINUX, "Standard_LRS",
        "", "50", "");
    JobIdentifier jobIdentifier = new JobIdentifier(2L);
    String environment = "env";
    CreateAgentRequest request = new CreateAgentRequest("key",
        elasticProfile,
        environment,
        jobIdentifier);
    ServerInfo serverInfo = mock(ServerInfo.class);

    VirtualMachine mockVM = mock(VirtualMachine.class, Mockito.RETURNS_DEEP_STUBS);
    AzureInstance mappedAzureInstance = mock(AzureInstance.class);
    when(mapper.map(mockVM)).thenReturn(mappedAzureInstance);
    when(mockGoCDAzureClient.createVM(vmConfigCaptor.capture())).thenReturn(mockVM);
    when(mockGoCDAzureClient.runCustomScript(any())).thenReturn("execution logs");
    when(serverInfo.getServerId()).thenReturn("server_id");
    when(mockVM.name()).thenReturn("vmName");
    PluginSettings settings = createPluginSettings();

    InOrder inOrder = inOrder(mockGoCDAzureClient);
    AzureInstance azureInstance = azureInstanceManager.create(mockGoCDAzureClient, request, settings, serverInfo);

    assertEquals(mappedAzureInstance, azureInstance);
    VmConfig actualVmConfig = vmConfigCaptor.getValue();

    assertNotNull(actualVmConfig);
    assertEquals(settings.getGoServerUrl(), actualVmConfig.getAgentConfig().getServerUrl());
    assertEquals("networkId", actualVmConfig.getNetworkId());
    assertEquals("subnet", actualVmConfig.getSubnet());
    assertEquals("Standard_D3_v2", actualVmConfig.getSize());
    assertEquals("nsg-123", actualVmConfig.getNetworkSecurityGroupId());
    assertEquals(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS.imageReference().id(), actualVmConfig.getImageReference().id());
    assertEquals(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS.imageReference().publisher(), actualVmConfig.getImageReference().publisher());

    inOrder.verify(mockGoCDAzureClient).installGoAgent(actualVmConfig);
    inOrder.verify(mockGoCDAzureClient).runCustomScript(actualVmConfig);
    inOrder.verify(mockGoCDAzureClient).startAgent(actualVmConfig);

    assertEquals("execution logs", azureInstanceManager.getExecutionLogs(actualVmConfig.getName()));
  }

  @Test
  void shouldTerminateAzureInstance() {
    AzureInstance instance = mock(AzureInstance.class);
    when(instance.getId()).thenReturn("vmId");

    azureInstanceManager.terminate(mockGoCDAzureClient, instance);
    verify(mockGoCDAzureClient).terminate("vmId");
  }

  @Test
  void shouldListAzureInstances() {
    VirtualMachine mockVm1 = mock(VirtualMachine.class, Mockito.RETURNS_DEEP_STUBS);
    VirtualMachine mockVm2 = mock(VirtualMachine.class, Mockito.RETURNS_DEEP_STUBS);
    AzureInstance mockAzureInstance1 = mock(AzureInstance.class);
    AzureInstance mockAzureInstance2 = mock(AzureInstance.class);

    when(mapper.map(mockVm1)).thenReturn(mockAzureInstance1);
    when(mapper.map(mockVm2)).thenReturn(mockAzureInstance2);
    when(mockGoCDAzureClient.runningVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, "server_id")).thenReturn(Arrays.asList(mockVm1, mockVm2));

    List<AzureInstance> azureInstances = azureInstanceManager.listInstances(mockGoCDAzureClient, "server_id");

    assertEquals(2, azureInstances.size());
    assertEquals(mockAzureInstance1, azureInstances.get(0));
    assertEquals(mockAzureInstance2, azureInstances.get(1));
  }

  @Test
  void shouldAddTagToTheAgent() {
    AzureInstance instance = mock(AzureInstance.class);
    AzureInstance expectedInstanceWithTags = mock(AzureInstance.class);
    when(instance.getId()).thenReturn("vm-id");
    VirtualMachine vmWithTags = mock(VirtualMachine.class, RETURNS_DEEP_STUBS);
    when(mockGoCDAzureClient.addTag("vm-id", "tag-1", "value-1"))
        .thenReturn(vmWithTags);
    when(mapper.map(vmWithTags)).thenReturn(expectedInstanceWithTags);

    AzureInstance instanceWithTags = azureInstanceManager.addTag(mockGoCDAzureClient, instance, "tag-1", "value-1");

    verify(mockGoCDAzureClient, times(1)).addTag("vm-id", "tag-1", "value-1");
    assertEquals(expectedInstanceWithTags, instanceWithTags);
  }


  @Test
  void shouldRemoveTagFromAgent() {
    AzureInstance instance = mock(AzureInstance.class);
    AzureInstance expectedInstanceWithoutTag = mock(AzureInstance.class);
    when(instance.getId()).thenReturn("vm-id");
    VirtualMachine vmWithoutTag = mock(VirtualMachine.class, RETURNS_DEEP_STUBS);
    when(mockGoCDAzureClient.removeTag("vm-id", "tag-1"))
        .thenReturn(vmWithoutTag);
    when(mapper.map(vmWithoutTag)).thenReturn(expectedInstanceWithoutTag);

    AzureInstance instanceWithoutTag = azureInstanceManager.removeTag(mockGoCDAzureClient, instance, "tag-1");

    verify(mockGoCDAzureClient, times(1)).removeTag("vm-id", "tag-1");
    assertEquals(expectedInstanceWithoutTag, instanceWithoutTag);
  }

  @Test
  void shouldTerminateVmsWithFailedProvisioningState() {
    VirtualMachine failedVm1 = mock(VirtualMachine.class);
    VirtualMachine failedVm2 = mock(VirtualMachine.class);
    List<VirtualMachine> failedVms = Arrays.asList(failedVm1, failedVm2);

    when(mockGoCDAzureClient.failedProvisioningStateVirtualMachinesWithTag(GOCD_SERVER_ID_TAG_KEY, "serverId")).thenReturn(failedVms);

    azureInstanceManager.terminateProvisionFailedVms(mockGoCDAzureClient, "serverId");

    verify(mockGoCDAzureClient).terminate(failedVm1);
    verify(mockGoCDAzureClient).terminate(failedVm2);
  }
}
