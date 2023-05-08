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
import java.util.Map;
import lombok.Getter;
import org.joda.time.Period;

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
  @SerializedName("api_user")
  private String apiUser;

  @Expose
  @SerializedName("api_key")
  private String apiKey;

  @Expose
  @SerializedName("api_url")
  private String apiUrl;

  private Period autoRegisterPeriod;

  public PluginSettings() {
  }

  public PluginSettings(String goServerUrl, String autoRegisterTimeout, String apiUser, String apiKey, String apiUrl, Period autoRegisterPeriod) {
    this.goServerUrl = goServerUrl;
    this.autoRegisterTimeout = autoRegisterTimeout;
    this.apiUser = apiUser;
    this.apiKey = apiKey;
    this.apiUrl = apiUrl;
    this.autoRegisterPeriod = autoRegisterPeriod;
  }

  public static PluginSettings fromJSON(String json) {
    return GSON.fromJson(json, PluginSettings.class);
  }

  public static PluginSettings fromConfiguration(Map<String, String> pluginSettings) {
    return GSON.fromJson(GSON.toJson(pluginSettings), PluginSettings.class);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PluginSettings that = (PluginSettings) o;

    if (goServerUrl != null ? !goServerUrl.equals(that.goServerUrl) : that.goServerUrl != null) {
      return false;
    }
    if (autoRegisterTimeout != null ? !autoRegisterTimeout.equals(that.autoRegisterTimeout) : that.autoRegisterTimeout != null) {
      return false;
    }
    if (apiUser != null ? !apiUser.equals(that.apiUser) : that.apiUser != null) {
      return false;
    }
    if (apiKey != null ? !apiKey.equals(that.apiKey) : that.apiKey != null) {
      return false;
    }
    return apiUrl != null ? apiUrl.equals(that.apiUrl) : that.apiUrl == null;
  }

  @Override
  public int hashCode() {
    int result = goServerUrl != null ? goServerUrl.hashCode() : 0;
    result = 31 * result + (getAutoRegisterPeriod() != null ? getAutoRegisterPeriod().hashCode() : 0);
    result = 31 * result + (apiUser != null ? apiUser.hashCode() : 0);
    result = 31 * result + (apiKey != null ? apiKey.hashCode() : 0);
    result = 31 * result + (apiUrl != null ? apiUrl.hashCode() : 0);
    return result;
  }

  public Period getAutoRegisterPeriod() {
    if (this.autoRegisterPeriod == null) {
      this.autoRegisterPeriod = new Period().withMinutes(Integer.parseInt(getAutoRegisterTimeout()));
    }
    return this.autoRegisterPeriod;
  }

  private String getAutoRegisterTimeout() {
    if (autoRegisterTimeout == null) {
      autoRegisterTimeout = "10";
    }
    return autoRegisterTimeout;
  }

  public String getApiUser() {
    return apiUser;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public String getGoServerUrl() {
    return goServerUrl;
  }
}
