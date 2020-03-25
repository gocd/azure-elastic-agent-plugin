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

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import org.joda.time.Period;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.LINUX;
import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.WINDOWS;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VmConfigTest extends BaseTest {

  @Test
  void shouldBuildVmConfigFromRequestParamsSettingsAndServerInfoProperties() {
    PluginSettings pluginSettings = createPluginSettings();
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    JobIdentifier jobIdentifier = new JobIdentifier("pipelineName",
        12L,
        "pipelineLabel",
        "stageName",
        "stageCounter",
        "jobName",
        45l);
    ElasticProfile profile = new ElasticProfile("Standard_A0",
        "Canonical:UbuntuServer:14.04.4-LTS",
        "",
        "",
        LINUX,
        "Standard_LRS",
        "",
        "50", "");
    CreateAgentRequest request = new CreateAgentRequest("auto-register-key", profile, "Testing", jobIdentifier);

    when(mockServerInfo.getServerId()).thenReturn("unique-server-id");
    when(mockServerInfo.getServerVersion()).thenReturn("server-v2");

    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(pluginSettings)
        .setServerInfoParams(mockServerInfo)
        .build();

    //request
    assertEquals("auto-register-key", vmConfig.getAgentConfig().getAutoRegisterKey());
    assertEquals(vmConfig.getEnvironment(), vmConfig.getAgentConfig().getEnvironment());
    assertEquals("Testing", vmConfig.getEnvironment());
    Map<String, String> tags = vmConfig.getTags();
    assertEquals(4, tags.size());
    assertEquals("Testing", tags.get(ENVIRONMENT_TAG_KEY));
    assertEquals(request.elasticProfile().hash(), tags.get(ELASTIC_PROFILE_TAG_KEY));
    assertEquals(String.format("nic-%s", vmConfig.getName()), vmConfig.getNetworkInterfaceName());
    assertEquals(request.jobIdentifier(), vmConfig.getJobIdentifier());

    //ServerInfo params
    assertEquals("unique-server-id", tags.get(GOCD_SERVER_ID_TAG_KEY));
    assertEquals("server-v2", vmConfig.getAgentConfig().getVersion());

    //Elastic profile params
    assertEquals("Standard_A0", vmConfig.getSize());
    assertEquals(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS.imageReference().publisher(), vmConfig.getImageReference().publisher());
    assertEquals(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS.imageReference().offer(), vmConfig.getImageReference().offer());
    assertEquals(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS.imageReference().sku(), vmConfig.getImageReference().sku());
    assertEquals(StorageAccountTypes.STANDARD_LRS, vmConfig.getOsDiskStorageAccountType());
    assertEquals(50, vmConfig.getOsDiskSize().get().intValue());

    assertTrue(vmConfig.getName().startsWith(VmConfig.VM_NAME_PREFIX));
    assertEquals(vmConfig.getName(), vmConfig.getAgentConfig().getAgentId());

    //pluginSettings params
    assertEquals(pluginSettings.getGoServerUrl(), vmConfig.getAgentConfig().getServerUrl());
    assertEquals(pluginSettings.getNetworkId(), vmConfig.getNetworkId());
    assertEquals(pluginSettings.getRandomSubnet(), vmConfig.getSubnet());
    assertEquals(pluginSettings.getNetworkSecurityGroupId(), vmConfig.getNetworkSecurityGroupId());

    assertEquals(pluginSettings.getResourceGroup(), vmConfig.getResourceGroup());
    assertEquals(pluginSettings.getRegion(), vmConfig.getRegion());
    assertEquals(pluginSettings.getSshKey(), vmConfig.getSshKey());
    assertEquals(pluginSettings.getLinuxUserName(), vmConfig.getUserName());
  }

  @Test
  void shouldSetWindowsCredentialsBasedOnPlatform() {
    PluginSettings pluginSettings = createPluginSettings();
    JobIdentifier jobIdentifier = new JobIdentifier("pipelineName",
        12L,
        "pipelineLabel",
        "stageName",
        "stageCounter",
        "jobName",
        45l);
    ElasticProfile profile = new ElasticProfile("Standard_A0",
        "Canonical:UbuntuServer:14.04.4-LTS",
        "",
        "",
        WINDOWS, "Standard_LRS", "", "50", "");
    CreateAgentRequest request = new CreateAgentRequest("auto-register-key", profile, "Testing", jobIdentifier);

    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(pluginSettings)
        .build();

    assertEquals(pluginSettings.getWindowsPassword(), vmConfig.getPassword());
    assertEquals(pluginSettings.getWindowsUserName(), vmConfig.getUserName());
  }

  @Test
  void shouldSetDefaultRegionIfNotSet() {
    CreateAgentRequest request = new CreateAgentRequest("auto-register-key", null, "Testing", null);

    PluginSettings pluginSettings = new PluginSettings("serverUrl",
        "2", "15", "domain",
        "clientId", "secret", Period.minutes(2),
        "", "", "",
        "", "", "",
        "", "", "");
    VmConfig vmConfig = new VmConfig.Builder()
        .setSettingsParams(pluginSettings)
        .setRequestParams(request)
        .build();
    assertEquals(Region.US_WEST, vmConfig.getRegion());
  }

  @Test
  void shouldTrimAdditionalSpacesInCustomScript() {
    ElasticProfile profile = new ElasticProfile("Standard_A0",
        "Canonical:UbuntuServer:14.04.4-LTS",
        "",
        "Script with spaces          ",
        LINUX, "Standard_LRS", "", "50", "");
    CreateAgentRequest request = new CreateAgentRequest("auto-register-key",
        profile,
        "Testing",
        new JobIdentifier(1L));

    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(createPluginSettings())
        .build();

    assertEquals("Script with spaces", vmConfig.getCustomScript());

  }

  @Test
  void shouldSetDefaultImageSizeIfNotElasticProfileSet() {
    CreateAgentRequest request = new CreateAgentRequest("auto-register-key", null, "Testing", null);
    VmConfig vmConfig = new VmConfig.Builder()
        .setSettingsParams(createPluginSettings())
        .setRequestParams(request)
        .build();
    assertEquals("Standard_D3_v2", vmConfig.getSize());
  }

  @Test
  void shouldSetDefaultImageSizeIfNotSet() {
    CreateAgentRequest request = new CreateAgentRequest("",
        new ElasticProfile(null,
            "Canonical:UbuntuServer:14.04.4-LTS",
            "",
            "",
            LINUX, "Standard_LRS", "", "50", ""),
        "",
        new JobIdentifier(1L));
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(createPluginSettings())
        .build();
    assertEquals("Standard_D3_v2", vmConfig.getSize());
  }

  @Test
  void shouldReturnEmptyStringWhenEnvironmentIsNull() {
    CreateAgentRequest request = new CreateAgentRequest("auto-register-key", null, null, null);
    VmConfig vmConfig = new VmConfig.Builder()
        .setSettingsParams(createPluginSettings())
        .setRequestParams(request)
        .build();
    assertEquals("", vmConfig.getEnvironment());
  }

  @Test
  void shouldImageReferenceToNullElasticProfileIsNotSet() {
    VmConfig vmConfig = new VmConfig.Builder()
        .setSettingsParams(createPluginSettings())
        .setRequestParams(new CreateAgentRequest("",
            null,
            "",
            new JobIdentifier(1L)))
        .build();

    assertNull(vmConfig.getImageReference());
  }

  @Test
  void shouldDefaultPlatformToLinuxElasticProfileIsNotSet() {
    CreateAgentRequest request = new CreateAgentRequest("auto-register-key", null, "Testing", null);
    VmConfig vmConfig = new VmConfig.Builder()
        .setSettingsParams(createPluginSettings())
        .setRequestParams(request)
        .build();

    assertEquals(LINUX, vmConfig.getPlatform());
  }

  @Test
  void shouldDefaultPlatformToLinuxIfNotSet() {
    CreateAgentRequest request = new CreateAgentRequest("",
        new ElasticProfile(null,
            "Canonical:UbuntuServer:14.04.4-LTS",
            "",
            "",
            LINUX, "Standard_LRS", "", "50", ""),
        "",
        new JobIdentifier(1L));
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(createPluginSettings())
        .build();

    assertEquals(LINUX, vmConfig.getPlatform());
  }

  @Test
  void shouldFetchPlatformStrategyFromPlatform() {
    Platform mockPlatform = mock(Platform.class);
    CreateAgentRequest request = new CreateAgentRequest("",
        new ElasticProfile(null,
            "Canonical:UbuntuServer:14.04.4-LTS",
            "",
            "",
            mockPlatform, "Standard_LRS", "", "50", ""),
        "",
        new JobIdentifier(1L));
    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(createPluginSettings())
        .build();
    LinuxPlatformConfigStrategy expectedStrategy = new LinuxPlatformConfigStrategy(new CustomScriptBuilder());
    when(mockPlatform.getConfigStrategy()).thenReturn(expectedStrategy);

    assertEquals(expectedStrategy, vmConfig.getPlatformStrategy());
    verify(mockPlatform).getConfigStrategy();
  }

  @Test
  void shouldAddIdleTimeoutTagIfValueSetInElasticProfile() {
    PluginSettings pluginSettings = createPluginSettings();
    ElasticProfile profile = new ElasticProfile("Standard_A0",
        "Canonical:UbuntuServer:14.04.4-LTS",
        "",
        "",
        WINDOWS, "Standard_LRS", "10", "50", "");
    CreateAgentRequest request = new CreateAgentRequest("", profile, "", null);

    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(pluginSettings)
        .build();

    assertEquals("10", vmConfig.getTags().get(VMTags.IDLE_TIMEOUT));

  }

  @Test
  void shouldAddSubnetValueSetInElasticProfile() {
    PluginSettings pluginSettings = createPluginSettings();
    ElasticProfile profile = new ElasticProfile("Standard_A0",
        "Canonical:UbuntuServer:14.04.4-LTS",
        "",
        "",
        WINDOWS, "Standard_LRS", "10", "50", "profile-subnet");
    CreateAgentRequest request = new CreateAgentRequest("", profile, "", null);

    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(pluginSettings)
        .build();

    assertEquals("profile-subnet", vmConfig.getSubnet());
  }

  @Test
  void shouldAddIdleTimeoutTagFromPluginSettingsIfValueNotDefinedInElasticProfile() {
    PluginSettings pluginSettings = createPluginSettings();
    ElasticProfile profile = new ElasticProfile("Standard_A0",
        "Canonical:UbuntuServer:14.04.4-LTS",
        "",
        "",
        WINDOWS, "Standard_LRS", "", "50", "");
    CreateAgentRequest request = new CreateAgentRequest("", profile, "", null);

    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(pluginSettings)
        .build();

    assertEquals("15", vmConfig.getTags().get(VMTags.IDLE_TIMEOUT));
  }

  @Test
  void shouldSubnetFromPluginSettingsIfValueNotDefinedInElasticProfile() {
    PluginSettings pluginSettings = createPluginSettings();
    ElasticProfile profile = new ElasticProfile("Standard_A0",
        "Canonical:UbuntuServer:14.04.4-LTS",
        "",
        "",
        WINDOWS, "Standard_LRS", "", "50", "");
    CreateAgentRequest request = new CreateAgentRequest("", profile, "", null);

    VmConfig vmConfig = new VmConfig.Builder()
        .setRequestParams(request)
        .setSettingsParams(pluginSettings)
        .build();

    assertEquals("subnet", vmConfig.getSubnet());
  }
}
