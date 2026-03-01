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

import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.AgentConfig;
import static com.thoughtworks.gocd.elasticagent.azure.Constants.DEFAULT_GO_SERVER_VERSION;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.ImageURN;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import static com.thoughtworks.gocd.elasticagent.azure.models.Platform.LINUX;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import static com.thoughtworks.gocd.elasticagent.azure.utils.Util.uniqueString;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.isBlank;
import org.joda.time.Period;

@Getter
public class VmConfig {

  public static final String VM_NAME_PREFIX = "gocd-azure";
  private final AgentConfig agentConfig;
  private final String environment;
  private final Region region;
  private final String resourceGroup;
  private final String networkId;
  private final String userName;
  private final String subnet;
  private final String name;
  private final String sshKey;
  private final String size;
  private final ImageReference imageReference;
  private final String customScript;
  private final Map<String, String> tags;
  private final String customImageId;
  private final String networkSecurityGroupId;
  private final Platform platform;
  private final String password;
  private final StorageAccountTypes osDiskStorageAccountType;
  private final Optional<Integer> osDiskSize;
  private final JobIdentifier jobIdentifier;

  @Override
  public String toString() {
    String imageReferenceString = "";
    if (imageReference != null) {
      imageReferenceString = String.format("{publisher:%s,offer:%s,sku:%s,version:%s}",
        imageReference.publisher(),
        imageReference.offer(),
        imageReference.sku(),
        imageReference.version());
    }
    return "VmConfig{"
      + "agentConfig=" + agentConfig
      + ", region=" + region
      + ", resourceGroup='" + resourceGroup + '\''
      + ", networkId='" + networkId + '\''
      + ", UserName='" + userName + '\''
      + ", subnet='" + subnet + '\''
      + ", name='" + name + '\''
      + ", size='" + size + '\''
      + ", osDiskStorageAccountType='" + osDiskStorageAccountType + '\''
      + ", osDiskSize='" + (osDiskSize.isPresent() ? osDiskSize.get() : "") + '\''
      + ", customImageId=" + customImageId
      + ", imageReference=" + imageReferenceString
      + ", tags=" + tags
      + '}';
  }

  // ToDo: Find server version, check vm name limit, How to specify Custom image id ?
  VmConfig(Builder builder) {
    this.name = uniqueString(VM_NAME_PREFIX);
    this.environment = builder.environment;
    this.agentConfig = new AgentConfig(builder.goServerUrl, builder.autoregisterKey, builder.serverVersion,
      this.environment, this.name);
    this.region = builder.region;
    this.resourceGroup = builder.resourceGroup;
    this.networkId = builder.networkId;
    this.subnet = builder.subnet;
    this.networkSecurityGroupId = builder.networkSecurityGroupId;
    this.size = builder.size;
    this.imageReference = builder.imageReference;
    this.customScript = builder.customScript;
    this.customImageId = builder.customImageId;
    this.platform = builder.platform;
    this.userName = builder.userName;
    this.sshKey = builder.sshKey;
    this.password = builder.windowsPassword;
    this.osDiskStorageAccountType = builder.osDiskStorageAccountType;
    this.osDiskSize = builder.osDiskSize;
    this.tags = builder.tags;
    this.jobIdentifier = builder.jobIdentifier;
  }

  public PlatformConfigStrategy getPlatformStrategy() {
    return platform.getConfigStrategy();
  }

  public String getNetworkInterfaceName() {
    return String.format("nic-%s", this.name);
  }

  public static class Builder {

    private CreateAgentRequest request;
    private PluginSettings settings;

    private Region region;
    public String serverVersion;
    private String customImageId;
    private StorageAccountTypes osDiskStorageAccountType;
    private Optional<Integer> osDiskSize;
    private String environment;
    private String autoregisterKey;
    private String goServerUrl;
    private String networkId;
    private String subnet;
    private String resourceGroup;
    private String windowsPassword;
    private String sshKey;
    private String networkSecurityGroupId;
    private String size;
    private Platform platform;
    private String userName;
    private ImageReference imageReference;
    private String customScript;
    private Map<String, String> tags = new HashMap<>();
    private JobIdentifier jobIdentifier;

    public VmConfig build() {
      this.goServerUrl = settings.getGoServerUrl();
      this.networkId = settings.getNetworkId();
      this.networkSecurityGroupId = settings.getNetworkSecurityGroupId();
      this.resourceGroup = settings.getResourceGroup();
      this.sshKey = settings.getSshKey();
      this.region = Optional.ofNullable(settings.getRegion()).orElse(Region.US_WEST);
      this.windowsPassword = settings.getWindowsPassword();

      this.environment = Optional.ofNullable(request.environment()).orElse("");
      this.autoregisterKey = request.autoRegisterKey();
      this.subnet = request.properties().get(ElasticProfile.SUBNET_NAME);
      this.size = request.properties().get(ElasticProfile.VM_SIZE);
      this.imageReference = new ImageURN(request.properties().get(ElasticProfile.VM_IMAGE_URN)).toImageReference();
      this.customImageId = request.properties().get(ElasticProfile.VM_CUSTOM_IMAGE_ID);
      this.osDiskStorageAccountType
        = isBlank(request.properties().get(ElasticProfile.OS_DISK_STORAGE_ACCOUNT_TYPE))
        ? StorageAccountTypes.STANDARD_SSD_LRS
        : StorageAccountTypes.fromString(request.properties().get(ElasticProfile.OS_DISK_STORAGE_ACCOUNT_TYPE));
      this.osDiskSize = isBlank(request.properties().get(ElasticProfile.OS_DISK_SIZE)) ? Optional.empty() : Optional.of(Integer.valueOf(request.properties().get(ElasticProfile.OS_DISK_SIZE)));
      this.customScript = request.properties().get(ElasticProfile.SUBNET_NAME);
      this.platform = Platform.valueOf(request.properties().get(ElasticProfile.PLATFORM));
      this.jobIdentifier = request.jobIdentifier();
      //overrides
      this.userName = LINUX.equals(platform) ? settings.getLinuxUserName() : settings.getWindowsUserName();

      tags.put(ENVIRONMENT_TAG_KEY, environment);
      tags.put(ELASTIC_PROFILE_TAG_KEY, request.getClusterProfileProperties().hash());
      tags.put(IDLE_TIMEOUT, String.format("%s", getIdleTimeoutPeriod(request.properties().get(ElasticProfile.IDLE_TIMEOUT))));
      return new VmConfig(this);
    }

    public Builder setSettingsParams(PluginSettings settings) {
      this.settings = settings;
      return this;
    }

    public Builder setServerInfoParams(ServerInfo serverInfo) {
      tags.put(GOCD_SERVER_ID_TAG_KEY, serverInfo.getServerId());
      this.serverVersion = Optional.ofNullable(serverInfo.getServerVersion()).orElse(DEFAULT_GO_SERVER_VERSION);
      return this;
    }

    public Period getIdleTimeoutPeriod(String idleTimeout) {
      if (StringUtils.isNotBlank(idleTimeout)) {
        return new Period().withMinutes(Integer.parseInt(idleTimeout));
      }
      return null;
    }

    public Builder setRequestParams(CreateAgentRequest request) {
      this.request = request;
      return this;
    }
  }
}
