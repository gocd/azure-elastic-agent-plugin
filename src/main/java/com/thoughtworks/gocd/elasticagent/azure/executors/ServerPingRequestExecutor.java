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

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.*;
import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ServerRequestFailedException;
import com.thoughtworks.gocd.elasticagent.azure.requests.ServerPingRequest;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;
import java.util.Collection;
import java.util.List;

public class ServerPingRequestExecutor implements RequestExecutor {

  private final ServerPingRequest request;
  private final AzureAgentInstances agentInstances;
  private final PluginRequest pluginRequest;
  private ServerHealthMessagingService serverHealthMessagingService;

  public ServerPingRequestExecutor(ServerPingRequest serverPingRequest, AzureAgentInstances agentInstances, PluginRequest pluginRequest, ServerHealthMessagingService serverHealthMessagingService) {
    this.request = serverPingRequest;
    this.agentInstances = agentInstances;
    this.pluginRequest = pluginRequest;
    this.serverHealthMessagingService = serverHealthMessagingService;
  }

  @Override
  public GoPluginApiResponse execute() throws Exception {
    List<ClusterProfileProperties> pluginSettings = request.allClusterProfileProperties();

    for (ClusterProfileProperties clusterProfileProperties : pluginSettings) {
      Agents allAgents = pluginRequest.listAgents();
      Agents missingAgents = new Agents();

      for (Agent agent : allAgents.agents()) {
        if (agentInstances.find(agent.elasticAgentId()) == null) {
          LOG.warn("Was expecting an instance with name " + agent.elasticAgentId() + ", but it was missing!");
          missingAgents.add(agent);
        }
      }

      Agents agentsToDisable = agentInstances.instancesToBeDisabled(clusterProfileProperties, allAgents);
      agentsToDisable.addAll(missingAgents);

      disableIdleAgents(agentsToDisable);

      allAgents = pluginRequest.listAgents();
      terminateDisabledAgents(allAgents, clusterProfileProperties);

      agentInstances.terminateUnregisteredInstances(clusterProfileProperties, allAgents);
      agentInstances.terminateProvisionFailedInstances(clusterProfileProperties, pluginRequest.getServerInfo());
      serverHealthMessagingService.clearExpiredHealthMessages();
    }

    return DefaultGoPluginApiResponse.success("");
  }

  private void disableIdleAgents(Agents agents) throws ServerRequestFailedException {
    pluginRequest.disableAgents(agents.findInstancesToDisable());
  }

  private void terminateDisabledAgents(Agents agents, PluginSettings pluginSettings) throws Exception {
    Collection<Agent> toBeDeleted = agents.findInstancesToTerminate();

    for (Agent agent : toBeDeleted) {
      agentInstances.terminate(agent.elasticAgentId(), pluginSettings);
    }

    pluginRequest.deleteAgents(toBeDeleted);
  }

}
