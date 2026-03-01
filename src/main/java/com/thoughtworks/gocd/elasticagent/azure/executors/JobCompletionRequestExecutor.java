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
import com.thoughtworks.gocd.elasticagent.azure.requests.JobCompletionRequest;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.JOB_IDENTIFIER_TAG_KEY;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.LAST_JOB_RUN_TAG_KEY;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JobCompletionRequestExecutor implements RequestExecutor {

  private final JobCompletionRequest jobCompletionRequest;
  private final AzureAgentInstances agentInstances;
  private final PluginRequest pluginRequest;
  private Clock clock;

  public JobCompletionRequestExecutor(JobCompletionRequest jobCompletionRequest,
    AzureAgentInstances agentInstances,
    PluginRequest pluginRequest,
    Clock clock) {
    this.jobCompletionRequest = jobCompletionRequest;
    this.agentInstances = agentInstances;
    this.pluginRequest = pluginRequest;
    this.clock = clock;
  }

  @Override
  public GoPluginApiResponse execute() throws Exception {
    PluginSettings pluginSettings = jobCompletionRequest.getClusterProfileProperties();
    String elasticAgentId = jobCompletionRequest.getElasticAgentId();
    Agent agent = new Agent(elasticAgentId);
    Agents agents = pluginRequest.listAgents();
    if (!agents.agentIds().contains(elasticAgentId)) {
      LOG.debug("[Job Completion] Skipping request to delete agent with id '{}' as the agent does not exist on the server.", elasticAgentId);
      return DefaultGoPluginApiResponse.success("");
    }
    AzureInstance instance = agentInstances.find(elasticAgentId);

    if (instance.isIdleAfterIdleTimeout()) {
      return terminateAgent(agent, pluginSettings);
    }
    return updateTags(elasticAgentId, pluginSettings);
  }

  private DefaultGoPluginApiResponse updateTags(String elasticAgentId, PluginSettings pluginSettings) throws IOException {
    String jobRunTime = String.valueOf(clock.now().toInstant().getMillis());
    agentInstances.addTag(pluginSettings, elasticAgentId, LAST_JOB_RUN_TAG_KEY, jobRunTime);
    agentInstances.removeTag(pluginSettings, elasticAgentId, JOB_IDENTIFIER_TAG_KEY);
    return DefaultGoPluginApiResponse.success("");

  }

  private DefaultGoPluginApiResponse terminateAgent(Agent agent, PluginSettings pluginSettings) throws Exception {
    LOG.debug("[Job Completion] Disabling elastic agent with id {} on job completion {}.", agent.elasticAgentId(), jobCompletionRequest.jobIdentifier());
    List<Agent> agents = Collections.singletonList(agent);
    pluginRequest.disableAgents(agents);

    LOG.debug("[Job Completion] Terminating elastic agent with id {} on job completion {}.", agent.elasticAgentId(), jobCompletionRequest.jobIdentifier());
    agentInstances.terminate(agent.elasticAgentId(), pluginSettings);

    LOG.debug("[Job Completion] Deleting elastic agent with id {} on job completion {}.", agent.elasticAgentId(), jobCompletionRequest.jobIdentifier());
    pluginRequest.deleteAgents(agents);
    return DefaultGoPluginApiResponse.success("");

  }
}
