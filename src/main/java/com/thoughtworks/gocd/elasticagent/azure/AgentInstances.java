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
package com.thoughtworks.gocd.elasticagent.azure;

import com.thoughtworks.gocd.elasticagent.azure.executors.ServerPingRequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.models.AgentStatusReport;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.models.StatusReport;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import java.io.IOException;

/**
 * Plugin implementors should implement these methods to interface to your
 * cloud. This interface is merely a suggestion for a very simple plugin. You
 * may change it to your needs.
 */
public interface AgentInstances<T> {

  /**
   * This message is sent to request creation of an agent instance.
   * Implementations may, at their discretion choose to not spin up an agent
   * instance.
   * <p>
   * So that instances created are auto-registered with the server, the agent
   * instance MUST have an <code>autoregister.properties</code> file.
   *
   * @param request the request object
   * @param settings   the plugin settings object
   * @param serverInfo the server info object
   */
  T create(CreateAgentRequest request, PluginSettings settings, ServerInfo serverInfo) throws Exception;

  /**
   * This message is sent when the plugin needs to terminate the agent instance.
   *
   * @param agentId the elastic agent id
   * @param settings the plugin settings object
   */
  void terminate(String agentId, PluginSettings settings) throws Exception;

  AzureInstance addTag(PluginSettings settings, String agentId, String tagName, String tagValue) throws IOException;

  /**
   * This message is sent from the {@link ServerPingRequestExecutor} to
   * terminate instances that did not register with the server after a timeout.
   * The timeout may be configurable and set via the {@link PluginSettings}
   * instance that is passed in.
   *
   * @param settings the plugin settings object
   * @param agents the list of all the agents
   */
  void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception;

  /**
   * This message is sent from the {@link ServerPingRequestExecutor} to filter
   * out any new agents, that have registered before the timeout period. The
   * timeout may be configurable and set via the {@link PluginSettings} instance
   * that is passed in.
   *
   * @param settings the plugin settings object
   * @param agents the list of all the agents
   * @return a list of agent instances which were created after
   * {@link PluginSettings#getAutoRegisterPeriod()} ago.
   */
  Agents instancesToBeDisabled(PluginSettings settings, Agents agents);

  /**
   * This message is sent after plugin initialization time so that the plugin
   * may connect to the cloud provider and fetch a list of all instances that
   * have been spun up by this plugin (before the server was shut down). This
   * call should be should ideally remember if the agent instances are
   * refreshed, and do nothing if instances were previously refreshed.
   *
   * @param pluginRequest the plugin request object
   */
  void refreshAll(ClusterProfileProperties clusterProfileProperties) throws Exception;

  /**
   * This Returns an agent instance with the specified <code>id</code> or
   * <code>null</code>, if the agent is not found.
   *
   * @param agentId the elastic agent id
   */
  T find(String agentId);

  /**
   * Finds an agent instance with the specified <code>jobIdentifier</code>
   *
   * @param jobIdentifier The Job Identifier
   * @return An agent instance, or <code>null</code> if the agent is not found
   */
  T find(JobIdentifier jobIdentifier);

  /**
   * Get the status report from the agents
   *
   * @param pluginSettings the plugin settings object
   * @return A StatusReport object
   * @throws Exception
   */
  StatusReport getStatusReport(PluginSettings pluginSettings) throws Exception;

  /**
   * Get the status report of an agent instance
   *
   * @param pluginSettings The plugin settings object
   * @param agentInstance The agent instance
   * @return An AgentStatusReport object
   */
  AgentStatusReport getAgentStatusReport(PluginSettings pluginSettings, T agentInstance);

  void removeTag(PluginSettings settings, String agentId, String tagName) throws Exception;
}
