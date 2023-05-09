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
package com.thoughtworks.gocd.elasticagent.azure.requests;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.ClusterProfileProperties;
import com.thoughtworks.gocd.elasticagent.azure.Constants;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.executors.CreateAgentRequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public class CreateAgentRequest {

  private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

  @SerializedName("auto_register_key")
  private String autoRegisterKey;

  @SerializedName("environment")
  private String environment;

  @SerializedName("job_identifier")
  private JobIdentifier jobIdentifier;

  private Map<String, String> elasticAgentProfileProperties;
  private ClusterProfileProperties clusterProfileProperties;

  public CreateAgentRequest(String autoRegisterKey, Map<String, String> elasticAgentProfileProperties, String environment, JobIdentifier jobIdentifier, Map<String, String> clusterProfileProperties) {
    this.autoRegisterKey = autoRegisterKey;
    this.elasticAgentProfileProperties = elasticAgentProfileProperties;
    this.environment = environment;
    this.jobIdentifier = jobIdentifier;
    this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileProperties);
  }

  public CreateAgentRequest(String autoRegisterKey, Map<String, String> elasticAgentProfileProperties, String environment, JobIdentifier jobIdentifier, ClusterProfileProperties clusterProfileProperties) {
    this.autoRegisterKey = autoRegisterKey;
    this.elasticAgentProfileProperties = elasticAgentProfileProperties;
    this.environment = environment;
    this.jobIdentifier = jobIdentifier;
    this.clusterProfileProperties = clusterProfileProperties;
  }

  public String autoRegisterKey() {
    return autoRegisterKey;
  }

  public String environment() {
    return environment;
  }

  public JobIdentifier jobIdentifier() {
    return jobIdentifier;
  }

  public Map<String, String> properties() {
    return elasticAgentProfileProperties;
  }

  public ClusterProfileProperties getClusterProfileProperties() {
    return clusterProfileProperties;
  }

  public static CreateAgentRequest fromJSON(String json) {
    return GSON.fromJson(json, CreateAgentRequest.class);
  }

  public RequestExecutor executor(AzureAgentInstances agentInstances, PluginRequest pluginRequest, RequestFingerprintCache requestFingerprintCache, ServerHealthMessagingService serverHealthMessagingService) {
    return new CreateAgentRequestExecutor(this, agentInstances, pluginRequest, requestFingerprintCache, serverHealthMessagingService);
  }

  public Properties autoregisterProperties(String elasticAgentId) {
    Properties properties = new Properties();

    if (StringUtils.isNotBlank(autoRegisterKey)) {
      properties.put("agent.auto.register.key", autoRegisterKey);
    }

    if (StringUtils.isNotBlank(environment)) {
      properties.put("agent.auto.register.environments", environment);
    }

    properties.put("agent.auto.register.elasticAgent.agentId", elasticAgentId);
    properties.put("agent.auto.register.elasticAgent.pluginId", Constants.PLUGIN_ID);

    return properties;
  }

}
