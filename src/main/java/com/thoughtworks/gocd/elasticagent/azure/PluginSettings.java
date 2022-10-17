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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;
import lombok.AccessLevel;
import lombok.Getter;
import org.joda.time.Period;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
public class PluginSettings {
  public static final Gson GSON = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .excludeFieldsWithoutExposeAnnotation()
      .create();

  @Expose
  @SerializedName("go_server_url")
  private String goServerUrl;

  @Expose
  @SerializedName("auto_register_timeout")
  private String autoRegisterTimeout;

  @Expose
  @SerializedName("idle_timeout")
  private String idleTimeout;

  @Expose
  @SerializedName("domain")
  private String domain;

  @Expose
  @SerializedName("client_id")
  private String clientId;

  @Expose
  @SerializedName("secret")
  private String secret;

  @Expose
  @SerializedName("network_id")
  private String networkId;

  @Expose
  @SerializedName("subnet")
  @Getter(AccessLevel.NONE)
  private String subnetNames;

  @Expose
  @SerializedName("network_security_group_id")
  private String networkSecurityGroupId;

  @Expose
  @SerializedName("resource_group")
  private String resourceGroup;

  @Expose
  @SerializedName("region_name")
  @Getter(AccessLevel.NONE)
  private String regionName;

  @Expose
  @SerializedName("linux_user_name")
  private String linuxUserName;

  @Expose
  @SerializedName("ssh_key")
  private String sshKey;

  @Expose
  @SerializedName("windows_user_name")
  private String windowsUserName;

  @Expose
  @SerializedName("windows_password")
  private String windowsPassword;


  private Period autoRegisterPeriod;
  private Period idleTimeoutPeriod;

  @Getter(AccessLevel.NONE)
  private String[] subnetNamesArray;

  public static PluginSettings fromJSON(String json) {
    return GSON.fromJson(json, PluginSettings.class);
  }

  public PluginSettings(String goServerUrl,
                        String autoRegisterTimeout,
                        String idleTimeout,
                        String domain,
                        String clientId,
                        String secret,
                        Period autoRegisterPeriod,
                        String networkId,
                        String subnetNames,
                        String networkSecurityGroupId,
                        String resourceGroup,
                        String sshKey,
                        String regionName,
                        String linuxUserName,
                        String windowsUserName,
                        String windowsPassword) {
    this.goServerUrl = goServerUrl;
    this.autoRegisterTimeout = autoRegisterTimeout;
    this.idleTimeout = idleTimeout;
    this.domain = domain;
    this.clientId = clientId;
    this.secret = secret;
    this.autoRegisterPeriod = autoRegisterPeriod;
    this.networkId = networkId;
    this.subnetNames = subnetNames;
    this.networkSecurityGroupId = networkSecurityGroupId;
    this.resourceGroup = resourceGroup;
    this.regionName = regionName;
    this.linuxUserName = linuxUserName;
    this.sshKey = sshKey;
    this.windowsUserName = windowsUserName;
    this.windowsPassword = windowsPassword;
  }


  public PluginSettings() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PluginSettings that = (PluginSettings) o;

    if (goServerUrl != null ? !goServerUrl.equals(that.goServerUrl) : that.goServerUrl != null) return false;
    if (autoRegisterTimeout != null ? !autoRegisterTimeout.equals(that.autoRegisterTimeout) : that.autoRegisterTimeout != null)
      return false;
    if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
    if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
    return secret != null ? secret.equals(that.secret) : that.secret == null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(goServerUrl, autoRegisterTimeout, domain, clientId, secret, autoRegisterPeriod);
  }

  public Period getAutoRegisterPeriod() {
    if (this.autoRegisterPeriod == null) {
      this.autoRegisterPeriod = new Period().withMinutes(Integer.parseInt(autoRegisterTimeout));
    }
    return this.autoRegisterPeriod;
  }

  public Period getIdleTimeoutPeriod() {
    if (this.idleTimeoutPeriod == null) {
      this.idleTimeoutPeriod = new Period().withMinutes(Integer.parseInt(getIdleTimeout()));
    }
    return this.idleTimeoutPeriod;
  }

  public Region getRegion() {
    return Region.findByLabelOrName(regionName);
  }

  public String[] getSubnetNames() {
    if (this.subnetNamesArray == null) {
      this.subnetNamesArray = Util.splitByComma(subnetNames);
    }
    return this.subnetNamesArray;
  }

  public String getRandomSubnet() {
    int index = (this.getSubnetNames().length == 0) ? 0 : Util.random((getSubnetNames().length));
    return getSubnetNames()[index];
  }

  private String getIdleTimeout() {
    return isBlank(idleTimeout) ? "0" : idleTimeout;
  }

}
