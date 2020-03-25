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
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.PluginSettingsNotConfiguredException;
import com.thoughtworks.gocd.elasticagent.azure.models.StatusReport;
import com.thoughtworks.gocd.elasticagent.azure.utils.TemplateReader;

import java.util.HashMap;
import java.util.Map;

public class StatusReportExecutor implements RequestExecutor {

  public static final String STATUS_REPORT_TEMPLATE = "plugin-status-report.template.ftlh";
  public static final String ERROR_TEMPLATE = "error.template.ftlh";
  private final PluginRequest pluginRequest;
  private final AzureAgentInstances agentInstances;
  private static final Logger LOG = Logger.getLoggerFor(AgentStatusReportExecutor.class);
  private final TemplateReader templateReader;

  public StatusReportExecutor(PluginRequest pluginRequest, AzureAgentInstances agentInstances, TemplateReader templateReader) {
    this.pluginRequest = pluginRequest;
    this.agentInstances = agentInstances;
    this.templateReader = templateReader;
  }

  @Override
  public GoPluginApiResponse execute() throws Exception {
    LOG.info("[status-report] Generating status report");
    try {
      agentInstances.refreshAll(pluginRequest);
      StatusReport statusReport = agentInstances.getStatusReport(pluginRequest.getPluginSettings());

      final String statusReportView = templateReader.read(STATUS_REPORT_TEMPLATE, statusReport);

      return constructApiResponse(statusReportView);
    } catch (PluginSettingsNotConfiguredException e) {
      final String statusReportView = templateReader.read(ERROR_TEMPLATE, errorMessage("Unable to generate status report", "Azure Plugin not configured"));
      return constructApiResponse(statusReportView);
    } catch (Exception e) {
      LOG.error("Failed to generate status report, {}", e.getMessage());
      final String statusReportView = templateReader.read(ERROR_TEMPLATE, errorMessage("Failed to generate status report", e.getMessage()));
      return constructApiResponse(statusReportView);
    }
  }

  private Map<String, String> errorMessage(String message, String description) {
    return new HashMap<String, String>(){{
      put("message", message);
      put("description", description);
    }};
  }

  private GoPluginApiResponse constructApiResponse(String statusReportView) {
    JsonObject responseJSON = new JsonObject();
    responseJSON.addProperty("view", statusReportView);
    return DefaultGoPluginApiResponse.success(responseJSON.toString());
  }
}
