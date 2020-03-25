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

import com.thoughtworks.gocd.elasticagent.azure.*;
import com.thoughtworks.gocd.elasticagent.azure.Agent.AgentState;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class ServerPingRequestExecutorTest extends BaseTest {

  @Mock
  AzureAgentInstances mockAgentInstances;

  @Mock
  PluginRequest mockPluginRequest;

  @Mock
  private ServerHealthMessagingService serverHealthMessagingService;

  @BeforeEach
  void setUp() {
    initMocks(this);
  }

  @Test
  void shouldDisableIdleMissingAgents() throws Exception {
    String agentId = UUID.randomUUID().toString();
    final Agents agents = new Agents(asList(getAgent(agentId, AgentState.Idle)));
    PluginSettings pluginSettings = createPluginSettings();
    when(mockPluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    when(mockPluginRequest.listAgents()).thenReturn(agents);
    when(mockAgentInstances.find(agentId)).thenReturn(null);
    when(mockAgentInstances.instancesToBeDisabled(pluginSettings, agents)).thenReturn(new Agents());
    verifyNoMoreInteractions(mockPluginRequest);

    new ServerPingRequestExecutor(mockAgentInstances, mockPluginRequest, serverHealthMessagingService).execute();

    verify(mockPluginRequest).disableAgents(argThat(collectionMatches(agents.agents())));
    verify(serverHealthMessagingService).clearExpiredHealthMessages();
  }

  @Test
  void shouldFindInstancesToBeDisabledAndDisableIdleAgents() throws Exception {
    String agent1 = "agent-1";
    String agent2 = "agent-2";
    Agent idleAgent = getAgent(agent1, AgentState.Idle);
    Agent buildingAgent = getAgent(agent2, AgentState.Building);
    final Agents agentsToBeDisabled = new Agents(asList(idleAgent, buildingAgent));
    final Agents idleAgents = new Agents(asList(idleAgent));
    PluginSettings pluginSettings = createPluginSettings();
    when(mockPluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    when(mockPluginRequest.listAgents()).thenReturn(agentsToBeDisabled);
    when(mockAgentInstances.find(agent1)).thenReturn(mock(AzureInstance.class));
    when(mockAgentInstances.find(agent2)).thenReturn(mock(AzureInstance.class));
    when(mockAgentInstances.instancesToBeDisabled(pluginSettings, agentsToBeDisabled)).thenReturn(agentsToBeDisabled);
    verifyNoMoreInteractions(mockPluginRequest);

    new ServerPingRequestExecutor(mockAgentInstances, mockPluginRequest, serverHealthMessagingService).execute();

    verify(mockPluginRequest).disableAgents(argThat(collectionMatches(idleAgents.agents())));
  }

  private Agent getAgent(String agentId, AgentState state) {
    return new Agent(agentId, state, Agent.BuildState.Unknown, Agent.ConfigState.Enabled);
  }

  @Test
  void testShouldTerminateAndDeleteDisabledAgents() throws Exception {
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    String agentId = UUID.randomUUID().toString();
    final Agents agents = new Agents(asList(new Agent(agentId, AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Disabled)));

    PluginSettings pluginSettings = createPluginSettings();
    when(mockPluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    when(mockPluginRequest.listAgents()).thenReturn(agents);
    when(mockPluginRequest.getServerInfo()).thenReturn(mockServerInfo);
    when(mockAgentInstances.find(agentId)).thenReturn(null);
    when(mockAgentInstances.instancesToBeDisabled(pluginSettings, agents)).thenReturn(new Agents());
    verifyNoMoreInteractions(mockPluginRequest);

    new ServerPingRequestExecutor(mockAgentInstances, mockPluginRequest, serverHealthMessagingService).execute();
    final Collection<Agent> values = agents.agents();

    verify(mockAgentInstances).terminate(agentId, pluginSettings);
    verify(mockAgentInstances).terminateProvisionFailedInstances(pluginSettings, mockServerInfo);
    verify(mockPluginRequest).deleteAgents(argThat(collectionMatches(values)));
  }

  @Test
  void testShouldTerminateInstancesThatNeverAutoRegistered() throws Exception {
    String agentId = UUID.randomUUID().toString();
    PluginSettings pluginSettings = createPluginSettings();
    when(mockPluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    Agents allAgents = new Agents();
    when(mockPluginRequest.listAgents()).thenReturn(allAgents);
    when(mockAgentInstances.find(agentId)).thenReturn(null);
    when(mockAgentInstances.instancesToBeDisabled(pluginSettings, allAgents)).thenReturn(new Agents());

    verifyNoMoreInteractions(mockPluginRequest);

    ServerPingRequestExecutor serverPingRequestExecutor = new ServerPingRequestExecutor(mockAgentInstances, mockPluginRequest, serverHealthMessagingService);
    serverPingRequestExecutor.execute();

    verify(mockAgentInstances).terminateUnregisteredInstances(pluginSettings, allAgents);
    verify(serverHealthMessagingService).clearExpiredHealthMessages();
  }


  private ArgumentMatcher<Collection<Agent>> collectionMatches(final Collection<Agent> values) {
    return argument -> new ArrayList<>(argument).equals(new ArrayList<>(values));
  }
}
