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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.AzureInstance;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.models.AgentStatusReport;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.requests.AgentStatusReportRequest;
import com.thoughtworks.gocd.elasticagent.azure.utils.TemplateReader;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class AgentStatusReportExecutor {
  private static final Logger LOG = Logger.getLoggerFor(AgentStatusReportExecutor.class);
  private final AgentStatusReportRequest request;
  private final PluginRequest pluginRequest;
  private final AzureAgentInstances agentInstances;
  private final TemplateReader templateReader;

  public AgentStatusReportExecutor(AgentStatusReportRequest request,
                                   PluginRequest pluginRequest,
                                   AzureAgentInstances agentInstances,
                                   TemplateReader templateReader) {
    this.request = request;
    this.pluginRequest = pluginRequest;
    this.agentInstances = agentInstances;
    this.templateReader = templateReader;
  }

  public GoPluginApiResponse execute() throws Exception {
    String elasticAgentId = request.getElasticAgentId();
    JobIdentifier jobIdentifier = request.getJobIdentifier();
    LOG.info(String.format("[status-report] Generating status report for agent: %s with job: %s", elasticAgentId, jobIdentifier));
    try {
      AzureInstance agentInstance = getAgentInstance(elasticAgentId, jobIdentifier);
      if (agentInstance != null) {
        AgentStatusReport agentStatusReport = agentInstances.getAgentStatusReport(pluginRequest.getPluginSettings(), agentInstance);
        final String statusReportView = templateReader.read("agent-status-report.template.ftlh", agentStatusReport);
        return constructResponseForReport(statusReportView);
      }
      return agentNotFoundApiResponse();
    } catch (Exception e) {
      LOG.debug("Exception while generating agent status report", e);
      final String statusReportView = templateReader.read("error.template.ftlh", new HashMap<>());
      return constructResponseForReport(statusReportView);
    }

  }

  private AzureInstance getAgentInstance(String elasticAgentId, JobIdentifier jobIdentifier) {
    return StringUtils.isNotBlank(elasticAgentId) ? agentInstances.find(elasticAgentId) : agentInstances.find(jobIdentifier);
  }

  private GoPluginApiResponse agentNotFoundApiResponse() throws Exception {
    final String statusReportView = templateReader.read("not-running.template.ftlh", new HashMap<>());
    return constructResponseForReport(statusReportView);
  }

  private GoPluginApiResponse constructResponseForReport(String statusReportView) {
    JsonObject responseJSON = new JsonObject();
    responseJSON.addProperty("view", statusReportView);
    return DefaultGoPluginApiResponse.success(responseJSON.toString());
  }
}
