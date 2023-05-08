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
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.executors.AgentStatusReportExecutor;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.utils.TemplateReader;
import java.util.Objects;

public class AgentStatusReportRequest {

  private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create();

  @Expose
  private String elasticAgentId;

  @Expose
  private JobIdentifier jobIdentifier;

  @Expose
  private ClusterProfileProperties clusterProfileProperties;

  public AgentStatusReportRequest() {
  }

  public AgentStatusReportRequest(String elasticAgentId, JobIdentifier jobIdentifier) {
    this.elasticAgentId = elasticAgentId;
    this.jobIdentifier = jobIdentifier;
  }

  public static AgentStatusReportRequest fromJSON(String json) {
    return GSON.fromJson(json, AgentStatusReportRequest.class);
  }

  public String getElasticAgentId() {
    return elasticAgentId;
  }

  public JobIdentifier getJobIdentifier() {
    return jobIdentifier;
  }

  public AgentStatusReportExecutor executor(PluginRequest pluginRequest, AzureAgentInstances agentInstances, TemplateReader templateReader) {
    return new AgentStatusReportExecutor(this, pluginRequest, agentInstances, templateReader);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentStatusReportRequest that = (AgentStatusReportRequest) o;
    return Objects.equals(elasticAgentId, that.elasticAgentId)
      && Objects.equals(jobIdentifier, that.jobIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elasticAgentId, jobIdentifier);
  }

  @Override
  public String toString() {
    return "AgentStatusReportRequest{"
      + "elasticAgentId='" + elasticAgentId + '\''
      + ", jobIdentifierHash=" + jobIdentifier
      + '}';
  }
}
