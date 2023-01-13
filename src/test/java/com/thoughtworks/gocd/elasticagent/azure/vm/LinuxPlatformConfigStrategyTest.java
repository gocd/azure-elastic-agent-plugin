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

package com.thoughtworks.gocd.elasticagent.azure.vm;

import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithCreate;
import com.thoughtworks.gocd.elasticagent.azure.AgentConfig;
import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.*;

import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.LINUX;
import static com.thoughtworks.gocd.elasticagent.azure.vm.LinuxPlatformConfigStrategy.LINUX_START_GO_AGENT_TEMPLATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class LinuxPlatformConfigStrategyTest extends BaseTest {

  private LinuxPlatformConfigStrategy linuxPlatformConfigStrategy;

  @Mock
  private CustomScriptBuilder mockCustomScriptBuilder;

  @Captor
  private ArgumentCaptor<Map<String, String>> paramCaptor;

  @BeforeEach
  void setUp() {
    openMocks(this);
    linuxPlatformConfigStrategy = new LinuxPlatformConfigStrategy(mockCustomScriptBuilder);
  }

  @Test
  void addOSShouldSetOSProperties() {
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(new CreateAgentRequest("",
            new ElasticProfile("Size",
                "",
                "imageId",
                "",
                LINUX, "Standard_LRS", "", "50", ""),
            "",
            new JobIdentifier()))
        .setSettingsParams(createPluginSettings()).build();
    VirtualMachine.DefinitionStages.WithOS withOS = Mockito.mock(VirtualMachine.DefinitionStages.WithOS.class, Mockito.RETURNS_DEEP_STUBS);
    WithCreate mockReturn = mock(WithCreate.class);
    when(withOS.withLinuxCustomImage("imageId")
        .withRootUsername("username")
        .withSsh("sshKey")
        .withOSDiskStorageAccountType(StorageAccountTypes.STANDARD_LRS)
        .withSize("Size"))
        .thenReturn(mockReturn);

    WithCreate withCreate = linuxPlatformConfigStrategy.addOS(withOS, vmConfig);

    assertEquals(mockReturn, withCreate);
  }

  @Test
  void addOSShouldSetImageUrnPropertiesWhenSetForLinux() {
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(new CreateAgentRequest("",
            new ElasticProfile("Size",
                "Canonical:UbuntuServer:14.04.4-LTS",
                "imageId",
                "",
                LINUX, "StandardSSD_LRS", "", "50", ""),
            "",
            new JobIdentifier()))
        .setSettingsParams(createPluginSettings()).build();
    ArgumentCaptor<ImageReference> captor = ArgumentCaptor.forClass(ImageReference.class);
    VirtualMachine.DefinitionStages.WithOS withOS = Mockito.mock(VirtualMachine.DefinitionStages.WithOS.class, Mockito.RETURNS_DEEP_STUBS);
    WithCreate mockReturn = mock(WithCreate.class);
    when(withOS.withSpecificLinuxImageVersion(captor.capture())
        .withRootUsername("username")
        .withSsh("sshKey")
        .withOSDiskStorageAccountType(StorageAccountTypes.STANDARD_SSD_LRS)
        .withSize("Size"))
        .thenReturn(mockReturn);

    WithCreate withCreate = linuxPlatformConfigStrategy.addOS(withOS, vmConfig);

    assertEquals(mockReturn, withCreate);
    assertEquals("Canonical", captor.getValue().publisher());
    assertEquals("UbuntuServer", captor.getValue().offer());
    assertEquals("14.04.4-LTS", captor.getValue().sku());
    assertEquals("latest", captor.getValue().version());
  }

  @Test
  void testGetExtensionsShouldReturnUserCustomScriptExtension() {
    PluginSettings pluginSettings = createPluginSettings();
    ServerInfo serverInfo = mock(ServerInfo.class);
    when(serverInfo.getServerVersion()).thenReturn("19.1.0-7703");
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(new CreateAgentRequest("register-key",
            new ElasticProfile("Size",
                "Canonical:UbuntuServer:14.04.4-LTS",
                "imageId",
                "custom-script-1",
                LINUX, "Standard_LRS", "", "50", ""),
            "Test",
            new JobIdentifier()))
        .setSettingsParams(pluginSettings)
        .setServerInfoParams(serverInfo)
        .build();

    List<AzureVMExtension> extensions = linuxPlatformConfigStrategy.getExtensions(vmConfig);
    assertEquals(1, extensions.size());
    AzureVMExtension extension = extensions.get(0);
    assertTrue(extension instanceof LinuxCustomScriptExtension);
    Map<String, String> installParams = ((LinuxCustomScriptExtension) extension).getInstallParams();

    Map<String, String> expectedInstallParams = new HashMap<String, String>() {{
      put("version", "19.1.0-7703");
      put("go_server_url", pluginSettings.getGoServerUrl());
      put("autoregister_key", "register-key");
      put("environment", "Test");
      put("plugin_id", Util.pluginId());
      put("agent_id", vmConfig.getName());
      put("jre_feature_version", "17");
    }};
    assertEquals(expectedInstallParams, installParams);
  }

  @Test
  void testRunScriptShouldRunShellScript() {
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    RunCommandResult mockResult = mock(RunCommandResult.class);

    when(mockVirtualMachines.runShellScript("groupName", "vmName", Collections.singletonList("custom"), new ArrayList<>())).thenReturn(mockResult);
    RunCommandResult actualResult = linuxPlatformConfigStrategy.runScript("groupName", "vmName", mockVirtualMachines, "custom");

    assertEquals(mockResult, actualResult);
  }

  @Test
  void testStartAgentShouldInvokeStartAgentScript() throws Exception {
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    RunCommandResult mockResult = mock(RunCommandResult.class);
    AgentConfig agentConfig = mock(AgentConfig.class);
    String serverURL = "https:localhost:8154/go";
    when(agentConfig.getServerUrl()).thenReturn(serverURL);

    when(mockCustomScriptBuilder.withScript(eq(LINUX_START_GO_AGENT_TEMPLATE), paramCaptor.capture())).thenReturn(mockCustomScriptBuilder);
    when(mockCustomScriptBuilder.build()).thenReturn("start agent script");
    when(mockVirtualMachines.runShellScript("groupName", "vmName", Collections.singletonList("start agent script"), Collections.emptyList())).thenReturn(mockResult);

    RunCommandResult actualResult = linuxPlatformConfigStrategy.startAgent("groupName", "vmName", mockVirtualMachines, agentConfig);

    Map<String, String> params = paramCaptor.getValue();
    verify(mockVirtualMachines).runShellScript("groupName", "vmName", Collections.singletonList("start agent script"), Collections.emptyList());
    assertEquals(Map.of("go_server_url", serverURL, "jre_feature_version", "17"), params);
    assertEquals(mockResult, actualResult);
  }

}
