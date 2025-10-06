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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.Clock;
import com.thoughtworks.gocd.elasticagent.azure.ClusterProfileProperties;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.executors.JobCompletionRequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import java.util.Map;

public class JobCompletionRequest {

  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create();
  @Expose
  @SerializedName("elastic_agent_id")
  private String elasticAgentId;

  @Expose
  @SerializedName("job_identifier")
  private JobIdentifier jobIdentifier;

  @Expose
  @SerializedName("elastic_agent_profile_properties")
  private Map<String, String> properties;

  @Expose
  @SerializedName("cluster_profile_properties")
  private ClusterProfileProperties clusterProfileProperties;

  public JobCompletionRequest() {
  }

  public JobCompletionRequest(String elasticAgentId, JobIdentifier jobIdentifier, Map<String, String> properties, Map<String, String> clusterProfile) {
    this.elasticAgentId = elasticAgentId;
    this.jobIdentifier = jobIdentifier;
    this.properties = properties;
    this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfile);
  }

  public JobCompletionRequest(String elasticAgentId, JobIdentifier jobIdentifier, Map<String, String> properties, ClusterProfileProperties clusterProfileProperties) {
    this.elasticAgentId = elasticAgentId;
    this.jobIdentifier = jobIdentifier;
    this.properties = properties;
    this.clusterProfileProperties = clusterProfileProperties;
  }

  public static JobCompletionRequest fromJSON(String json) {
    JobCompletionRequest jobCompletionRequest = GSON.fromJson(json, JobCompletionRequest.class);
    return jobCompletionRequest;
  }

  public String getElasticAgentId() {
    return elasticAgentId;
  }

  public JobIdentifier jobIdentifier() {
    return jobIdentifier;
  }

  public ClusterProfileProperties getClusterProfileProperties() {
    return clusterProfileProperties;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public RequestExecutor executor(AzureAgentInstances agentInstances, PluginRequest pluginRequest) {
    return new JobCompletionRequestExecutor(this, agentInstances, pluginRequest, Clock.DEFAULT);
  }

  @Override
  public String toString() {
    return "JobCompletionRequest{"
      + "elasticAgentId='" + elasticAgentId + '\''
      + ", jobIdentifier=" + jobIdentifier
      + ", properties=" + properties
      + ", clusterProfileProperties=" + clusterProfileProperties
      + '}';
  }
}
