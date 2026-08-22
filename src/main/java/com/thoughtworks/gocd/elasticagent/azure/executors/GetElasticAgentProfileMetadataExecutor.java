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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;

import java.util.ArrayList;
import java.util.List;

public class GetElasticAgentProfileMetadataExecutor implements RequestExecutor {
  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  public static final Metadata IDLE_TIMEOUT = new NonNegativeIntegerMetadata(ElasticProfile.IDLE_TIMEOUT, false, false);
  public static final Metadata PLATFORM = new Metadata(ElasticProfile.PLATFORM, true, false);
  public static final Metadata VM_IMAGE_URN = new Metadata(ElasticProfile.VM_IMAGE_URN, false, false);
  public static final Metadata VM_SIZE = new Metadata(ElasticProfile.VM_SIZE, true, false);
  public static final Metadata CUSTOM_SCRIPT = new Metadata(ElasticProfile.CUSTOM_SCRIPT, false, false);
  public static final Metadata VM_CUSTOM_IMAGE_ID = new Metadata(ElasticProfile.VM_CUSTOM_IMAGE_ID, false, false);
  public static final Metadata OS_DISK_STORAGE_ACCOUNT_TYPE = new Metadata(ElasticProfile.OS_DISK_STORAGE_ACCOUNT_TYPE, true, false);
  public static final Metadata OS_DISK_SIZE = new NonNegativeIntegerMetadata(ElasticProfile.OS_DISK_SIZE, false, false);
  public static final Metadata SUBNET_NAME = new Metadata(ElasticProfile.SUBNET_NAME, false, false);

  public static final List<Metadata> FIELDS = new ArrayList<>();

  static {
    FIELDS.add(IDLE_TIMEOUT);
    FIELDS.add(PLATFORM);
    FIELDS.add(VM_IMAGE_URN);
    FIELDS.add(VM_SIZE);
    FIELDS.add(VM_CUSTOM_IMAGE_ID);
    FIELDS.add(CUSTOM_SCRIPT);
    FIELDS.add(OS_DISK_STORAGE_ACCOUNT_TYPE);
    FIELDS.add(OS_DISK_SIZE);
    FIELDS.add(SUBNET_NAME);
  }

  @Override

  public GoPluginApiResponse execute() throws Exception {
    return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
  }
}
