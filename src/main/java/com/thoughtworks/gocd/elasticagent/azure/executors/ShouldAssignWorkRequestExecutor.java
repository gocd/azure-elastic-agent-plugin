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

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.AgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.AzureInstance;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.requests.ShouldAssignWorkRequest;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.JOB_IDENTIFIER_TAG_KEY;

public class ShouldAssignWorkRequestExecutor implements RequestExecutor {
  private final AgentInstances<AzureInstance> agentInstances;
  private PluginSettings pluginSettings;
  private ServerHealthMessagingService serverHealthMessagingService;
  private final ShouldAssignWorkRequest request;

  public ShouldAssignWorkRequestExecutor(ShouldAssignWorkRequest request, AgentInstances<AzureInstance> agentInstances, PluginSettings pluginSettings, ServerHealthMessagingService serverHealthMessagingService) {
    this.request = request;
    this.agentInstances = agentInstances;
    this.pluginSettings = pluginSettings;
    this.serverHealthMessagingService = serverHealthMessagingService;
  }

  @Override
  public GoPluginApiResponse execute() {
    try {
      AzureInstance instance = agentInstances.find(request.agent().elasticAgentId());

      if (instance == null) {
        return DefaultGoPluginApiResponse.success("false");
      }

      if (instance.canBeAssigned(request.elasticProfile())) {
        agentInstances.addTag(pluginSettings, instance.getName(), JOB_IDENTIFIER_TAG_KEY, request.jobIdentifier().hash());
        serverHealthMessagingService.clearHealthMessage(request.jobIdentifier().getJobRepresentation());
        return DefaultGoPluginApiResponse.success("true");
      }

      return DefaultGoPluginApiResponse.success("false");
    } catch (Exception e) {
      LOG.error("Failed to check if agent can be assigned: {}", e.getMessage());
      return DefaultGoPluginApiResponse.success("false");
    }
  }
}
