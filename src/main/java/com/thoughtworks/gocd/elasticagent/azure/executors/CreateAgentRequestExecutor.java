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
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ProvisionFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.PluginHealthMessage;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import com.thoughtworks.gocd.elasticagent.azure.requests.RequestFingerprintCache;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;

public class CreateAgentRequestExecutor implements RequestExecutor {
  private final AzureAgentInstances agentInstances;
  private final PluginRequest pluginRequest;
  private RequestFingerprintCache requestFingerprintCache;
  private final CreateAgentRequest request;
  private ServerHealthMessagingService serverHealthMessagingService;

  public CreateAgentRequestExecutor(CreateAgentRequest request,
                                    AzureAgentInstances agentInstances,
                                    PluginRequest pluginRequest,
                                    RequestFingerprintCache requestFingerprintCache, ServerHealthMessagingService serverHealthMessagingService) {
    this.request = request;
    this.agentInstances = agentInstances;
    this.pluginRequest = pluginRequest;
    this.requestFingerprintCache = requestFingerprintCache;
    this.serverHealthMessagingService = serverHealthMessagingService;
  }

  @Override
  public GoPluginApiResponse execute() throws Exception {
    String requestFingerprint = request.jobIdentifier().hash();
    PluginSettings pluginSettings = pluginRequest.getPluginSettings();
    ServerInfo serverInfo = pluginRequest.getServerInfo();
    try {
      requestFingerprintCache.getOrExecute(requestFingerprint,
          pluginSettings.getAutoRegisterPeriod(),
          () -> {
            agentInstances.create(request, pluginSettings, serverInfo);
            serverHealthMessagingService.clearHealthMessage(request.jobIdentifier().getJobRepresentation());
          });
    } catch (ProvisionFailedException e) {
      serverHealthMessagingService.sendHealthMessage(e.jobRepresentation(), PluginHealthMessage.error(e.getMessage()));
      return DefaultGoPluginApiResponse.error(e.getMessage());
    }
    return new DefaultGoPluginApiResponse(200);
  }
}
