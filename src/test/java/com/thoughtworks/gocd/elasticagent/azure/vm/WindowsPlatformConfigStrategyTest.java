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

package com.thoughtworks.gocd.elasticagent.azure.vm;

import com.microsoft.azure.management.compute.*;
import com.thoughtworks.gocd.elasticagent.azure.AgentConfig;
import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.WINDOWS;
import static com.thoughtworks.gocd.elasticagent.azure.vm.WindowsPlatformConfigStrategy.WINDOWS_INSTALL_GO_AGENT_TEMPLATE;
import static com.thoughtworks.gocd.elasticagent.azure.vm.WindowsPlatformConfigStrategy.WINDOWS_START_GO_AGENT_TEMPLATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WindowsPlatformConfigStrategyTest extends BaseTest {

  @Mock
  CustomScriptBuilder mockCustomScriptBuilder;

  @Captor
  private ArgumentCaptor<Map<String, String>> paramsCaptor;

  private WindowsPlatformConfigStrategy windowsPlatformConfigStrategy;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    windowsPlatformConfigStrategy = new WindowsPlatformConfigStrategy(mockCustomScriptBuilder);
  }

  @Test
  void shouldAddWindowsOSProperties() {
    PluginSettings pluginSettings = createPluginSettings();
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(new CreateAgentRequest("",
            new ElasticProfile("Size",
                "",
                "imageId",
                "",
                WINDOWS, "Standard_LRS", "", "50", ""),
            "",
            new JobIdentifier()))
        .setSettingsParams(pluginSettings).build();
    VirtualMachine.DefinitionStages.WithOS withOS = Mockito.mock(VirtualMachine.DefinitionStages.WithOS.class, Mockito.RETURNS_DEEP_STUBS);
    VirtualMachine.DefinitionStages.WithCreate mockReturn = mock(VirtualMachine.DefinitionStages.WithCreate.class);
    when(withOS.withWindowsCustomImage("imageId")
        .withAdminUsername(pluginSettings.getWindowsUserName())
        .withAdminPassword(pluginSettings.getWindowsPassword())
        .withOSDiskStorageAccountType(StorageAccountTypes.STANDARD_LRS)
        .withSize("Size"))
        .thenReturn(mockReturn);

    VirtualMachine.DefinitionStages.WithCreate withCreate = windowsPlatformConfigStrategy.addOS(withOS, vmConfig);

    assertEquals(mockReturn, withCreate);
  }

  @Test
  void addOSShouldSetImageUrnPropertiesWhenSetForWindows() {
    PluginSettings pluginSettings = createPluginSettings();
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(new CreateAgentRequest(
            "",
            new ElasticProfile("Size",
                "MicrosoftWindowsServer:WindowsServer:2016-Datacenter",
                "",
                "",
                WINDOWS, "Premium_LRS", "", "50", ""),
            "",
            new JobIdentifier()))
        .setSettingsParams(pluginSettings).build();
    ArgumentCaptor<ImageReference> captor = ArgumentCaptor.forClass(ImageReference.class);
    VirtualMachine.DefinitionStages.WithOS withOS = Mockito.mock(VirtualMachine.DefinitionStages.WithOS.class, Mockito.RETURNS_DEEP_STUBS);
    VirtualMachine.DefinitionStages.WithCreate mockReturn = mock(VirtualMachine.DefinitionStages.WithCreate.class);
    when(withOS.withSpecificWindowsImageVersion(captor.capture())
        .withAdminUsername(pluginSettings.getWindowsUserName())
        .withAdminPassword(pluginSettings.getWindowsPassword())
        .withOSDiskStorageAccountType(StorageAccountTypes.PREMIUM_LRS)
        .withSize("Size"))
        .thenReturn(mockReturn);

    VirtualMachine.DefinitionStages.WithCreate withCreate = windowsPlatformConfigStrategy.addOS(withOS, vmConfig);

    assertEquals(mockReturn, withCreate);
    assertEquals("MicrosoftWindowsServer", captor.getValue().publisher());
    assertEquals("WindowsServer", captor.getValue().offer());
    assertEquals("2016-Datacenter", captor.getValue().sku());
    assertEquals("latest", captor.getValue().version());
  }

  @Test
  void testGetExtensionsShouldReturnNoExtensions() {
    assertTrue(windowsPlatformConfigStrategy.getExtensions(null).isEmpty());
  }

  @Test
  void testRunScriptShouldRunPowershellScript() {
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    RunCommandResult mockResult = mock(RunCommandResult.class);

    when(mockVirtualMachines.runPowerShellScript("groupName", "vmName", Collections.singletonList("custom"), new ArrayList<>())).thenReturn(mockResult);
    RunCommandResult actualResult = windowsPlatformConfigStrategy.runScript("groupName", "vmName", mockVirtualMachines, "custom");

    assertEquals(mockResult, actualResult);
  }

  @Test
  void testStartAgentShouldInvokeStartAgentScript() {
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    RunCommandResult mockResult = mock(RunCommandResult.class);

    when(mockVirtualMachines.runPowerShellScript("groupName", "vmName", Collections.singletonList("start agent script"), Collections.emptyList())).thenReturn(mockResult);
    when(mockCustomScriptBuilder.withScript(WINDOWS_START_GO_AGENT_TEMPLATE, Collections.emptyMap())).thenReturn(mockCustomScriptBuilder);
    when(mockCustomScriptBuilder.build()).thenReturn("start agent script");

    RunCommandResult actualResult = windowsPlatformConfigStrategy.startAgent("groupName", "vmName", mockVirtualMachines, null);

    verify(mockCustomScriptBuilder).withScript(WINDOWS_START_GO_AGENT_TEMPLATE, Collections.emptyMap());
    verify(mockVirtualMachines).runPowerShellScript("groupName", "vmName", Collections.singletonList("start agent script"), Collections.emptyList());
    assertEquals(mockResult, actualResult);
  }

  @Test
  void testInstallAgentShouldInvokeInstallAgentScriptWithCorrectParams() {
    VirtualMachines mockVirtualMachines = mock(VirtualMachines.class);
    VmConfig mockVmConfig = mock(VmConfig.class);
    when(mockVmConfig.getAgentConfig()).thenReturn(new AgentConfig("serverurl", "autoregisterkey", "version", "environment", "agentId"));
    when(mockVmConfig.getUserName()).thenReturn("username");
    when(mockVmConfig.getPassword()).thenReturn("password");
    when(mockVmConfig.getResourceGroup()).thenReturn("groupName");
    when(mockVmConfig.getName()).thenReturn("vmName");

    when(mockCustomScriptBuilder.withScript(eq(WINDOWS_INSTALL_GO_AGENT_TEMPLATE), paramsCaptor.capture())).thenReturn(mockCustomScriptBuilder);
    when(mockCustomScriptBuilder.build()).thenReturn("install script");
    RunCommandResult mockCommandResult = mock(RunCommandResult.class, RETURNS_DEEP_STUBS);
    when(mockVirtualMachines.runPowerShellScript("groupName", "vmName", Collections.singletonList("install script"), Collections.emptyList())).thenReturn(mockCommandResult);
    windowsPlatformConfigStrategy.installGoAgent(mockVirtualMachines, mockVmConfig);

    Map<String, String> actualParams = paramsCaptor.getValue();
    Map<String, String> expectedParams = new HashMap<>() {{
      put("go_server_url", "serverurl");
      put("autoregister_key", "autoregisterkey");
      put("environment", "environment");
      put("plugin_id", Util.pluginId());
      put("agent_id", "agentId");
      put("username", "username");
      put("password", "password");
      put("go_agent_installer_url", "https://download.gocd.org/binaries/20.5.0-11820/win/go-agent-20.5.0-11820-jre-64bit-setup.exe");
    }};
    assertEquals(expectedParams, actualParams);
    verify(mockVirtualMachines).runPowerShellScript("groupName", "vmName", Collections.singletonList("install script"), Collections.emptyList());
    verify(mockCustomScriptBuilder).withScript(WINDOWS_INSTALL_GO_AGENT_TEMPLATE, expectedParams);
  }

}
