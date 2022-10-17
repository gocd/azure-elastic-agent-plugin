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

package com.thoughtworks.gocd.elasticagent.azure.models;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class ServerInfo {

  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();

  @Expose
  @SerializedName("server_id")
  private String serverId;

  @Expose
  @SerializedName("site_url")
  private String siteUrl;

  @Expose
  @SerializedName("secure_site_url")
  private String secureSiteUrl;

  @Expose
  @SerializedName("go_version")
  private String goVersion;

  @Expose
  @SerializedName("dist_version")
  private String distVersion;

  @Expose
  @SerializedName("git_revision")
  private String gitRevision;


  public static ServerInfo fromJSON(String json) {
    return GSON.fromJson(json, ServerInfo.class);
  }

  public String getServerVersion(){
    return StringUtils.isNotBlank(this.goVersion) && StringUtils.isNotBlank(this.distVersion) ?
            String.format("%s-%s", this.goVersion, this.distVersion)
            : null;
  }

}
