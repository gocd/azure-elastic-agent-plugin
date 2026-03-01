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
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.models.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetClusterProfileMetadataExecutor implements RequestExecutor {

  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  public static final Field GO_SERVER_URL = new NonBlankField("go_server_url", "Go Server URL", null, true, false, "0");
  public static final Field AUTOREGISTER_TIMEOUT = new PositiveNumberField("auto_register_timeout", "Agent auto-register Timeout (in minutes)", "10", false, false, "1");
  public static final Field IDLE_TIMEOUT = new NonNegativeNumberField("idle_timeout", "Agent idle Timeout (in minutes)", "0", false, false, "2");
  public static final Field RESOURCE_GROUP = new NonBlankField("resource_group", "Resource Group", null, true, false, "3");
  public static final Field REGION_NAME = new NonBlankField("region_name", "Region Name", Region.US_WEST.toString(), true, false, "4");

  public static final Field NETWORK_ID = new NonBlankField("network_id", "Virtual Network Id", null, true, false, "5");
  public static final Field SUBNET_NAMES = new NonBlankField("subnet", "Subnet Name", null, true, false, "6");
  public static final Field NETWORK_SECURITY_GROUP_ID = new Field("network_security_group_id", "Network Security Group Id", null, false, false, "7");

  public static final Field CLIENT_ID = new NonBlankField("client_id", "Client Id", null, true, false, "8");
  public static final Field SECRET = new NonBlankField("secret", "Secret", null, true, true, "9");
  public static final Field DOMAIN = new NonBlankField("domain", "Domain/Tenant Id", null, true, false, "10");

  public static final Field LINUX_USER_NAME = new LinuxUsernameField("linux_user_name", "Linux User Name", null, true, false, "11");
  public static final Field SSH_KEY = new NonBlankField("ssh_key", "Ssh key", null, true, false, "12");

  public static final Field WINDOWS_USER_NAME = new WindowsUsernameField("windows_user_name", "Windows User Name", null, true, "13");
  public static final Field WINDOWS_PASSWORD = new WindowsPasswordField("windows_password", "Windows Password", null, true, "14");

  public static final Map<String, Field> CLUSTER_PROFILE_FIELDS = new LinkedHashMap<>();

  static {
    CLUSTER_PROFILE_FIELDS.put(GO_SERVER_URL.key(), GO_SERVER_URL);
    CLUSTER_PROFILE_FIELDS.put(AUTOREGISTER_TIMEOUT.key(), AUTOREGISTER_TIMEOUT);
    CLUSTER_PROFILE_FIELDS.put(IDLE_TIMEOUT.key(), IDLE_TIMEOUT);

    CLUSTER_PROFILE_FIELDS.put(LINUX_USER_NAME.key(), LINUX_USER_NAME);
    CLUSTER_PROFILE_FIELDS.put(SSH_KEY.key(), SSH_KEY);

    CLUSTER_PROFILE_FIELDS.put(WINDOWS_USER_NAME.key(), WINDOWS_USER_NAME);
    CLUSTER_PROFILE_FIELDS.put(WINDOWS_PASSWORD.key(), WINDOWS_PASSWORD);

    CLUSTER_PROFILE_FIELDS.put(DOMAIN.key(), DOMAIN);
    CLUSTER_PROFILE_FIELDS.put(CLIENT_ID.key(), CLIENT_ID);
    CLUSTER_PROFILE_FIELDS.put(SECRET.key(), SECRET);
    CLUSTER_PROFILE_FIELDS.put(RESOURCE_GROUP.key(), RESOURCE_GROUP);
    CLUSTER_PROFILE_FIELDS.put(REGION_NAME.key(), REGION_NAME);
    CLUSTER_PROFILE_FIELDS.put(NETWORK_ID.key(), NETWORK_ID);
    CLUSTER_PROFILE_FIELDS.put(SUBNET_NAMES.key(), SUBNET_NAMES);
    CLUSTER_PROFILE_FIELDS.put(NETWORK_SECURITY_GROUP_ID.key(), NETWORK_SECURITY_GROUP_ID);
  }

  public GoPluginApiResponse execute() {
    return new DefaultGoPluginApiResponse(200, GSON.toJson(CLUSTER_PROFILE_FIELDS));
  }

}
