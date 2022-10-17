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

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.*;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.requests.JobCompletionRequest;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.List;

import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.JOB_IDENTIFIER_TAG_KEY;
import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.LAST_JOB_RUN_TAG_KEY;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class JobCompletionRequestExecutorTest {
  @Mock
  private PluginRequest mockPluginRequest;
  @Mock
  private AzureAgentInstances mockAgentInstances;
  @Mock
  private Clock mockClock;
  @Captor
  private ArgumentCaptor<List<Agent>> agentsArgumentCaptor;


  @BeforeEach
  void setUp() {
    openMocks(this);
  }

  @Test
  void shouldAddLastJobRunTagOnJobCompletionAndRemoveJobIdentifierTagWithoutTerminatingTheAgent() throws Exception {
    JobIdentifier jobIdentifier = new JobIdentifier("test", 1L, "test", "test_stage", "1", "test_job", 100L);
    PluginSettings pluginSettings = mock(PluginSettings.class);
    String elasticAgentId = "agent-1";
    JobCompletionRequest request = new JobCompletionRequest(elasticAgentId, jobIdentifier);
    JobCompletionRequestExecutor executor = new JobCompletionRequestExecutor(request, mockAgentInstances, mockPluginRequest, mockClock);
    when(mockPluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    when(mockPluginRequest.listAgents())
        .thenReturn(new Agents(asList(new Agent(elasticAgentId), new Agent("some-other-agent"))));
    AzureInstance mockAzureInstance = mock(AzureInstance.class);
    when(mockAzureInstance.isIdleAfterIdleTimeout()).thenReturn(false);
    when(mockAgentInstances.find(elasticAgentId)).thenReturn(mockAzureInstance);
    DateTime currentDateTimeInUTC = DateTime.now();
    when(mockClock.now()).thenReturn(currentDateTimeInUTC);

    GoPluginApiResponse response = executor.execute();

    InOrder inOrder = inOrder(mockPluginRequest, mockAgentInstances);

    inOrder.verify(mockAgentInstances).addTag(pluginSettings, elasticAgentId, LAST_JOB_RUN_TAG_KEY,
        String.valueOf(currentDateTimeInUTC.toInstant().getMillis()));
    inOrder.verify(mockAgentInstances).removeTag(pluginSettings, elasticAgentId, JOB_IDENTIFIER_TAG_KEY);
    verify(mockAgentInstances, never()).terminate(elasticAgentId, pluginSettings);

    assertEquals(200, response.responseCode());
    assertTrue(response.responseBody().isEmpty());
  }

  @Test
  void shouldSkipAddingTagToANonExistingAgent() throws Exception {
    JobIdentifier jobIdentifier = new JobIdentifier("test", 1L, "test", "test_stage", "1", "test_job", 100L);
    PluginSettings pluginSettings = new PluginSettings();
    String elasticAgentId = "agent-1";
    JobCompletionRequest request = new JobCompletionRequest(elasticAgentId, jobIdentifier);
    JobCompletionRequestExecutor executor = new JobCompletionRequestExecutor(request, mockAgentInstances, mockPluginRequest, mockClock);
    when(mockPluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    when(mockPluginRequest.listAgents()).thenReturn(new Agents());
    GoPluginApiResponse response = executor.execute();

    verifyNoInteractions(mockAgentInstances);
    assertEquals(200, response.responseCode());
  }

  @Test
  void shouldTerminateAgentIfIdleTimeoutIsCompleted() throws Exception {
    JobIdentifier jobIdentifier = new JobIdentifier("test", 1L, "test", "test_stage", "1", "test_job", 100L);
    PluginSettings pluginSettings = mock(PluginSettings.class);
    String elasticAgentId = "agent-1";
    JobCompletionRequest request = new JobCompletionRequest(elasticAgentId, jobIdentifier);
    JobCompletionRequestExecutor executor = new JobCompletionRequestExecutor(request, mockAgentInstances, mockPluginRequest, mockClock);
    when(mockPluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    when(mockPluginRequest.listAgents())
        .thenReturn(new Agents(asList(new Agent(elasticAgentId), new Agent("some-other-agent"))));

    AzureInstance mockAzureInstance = mock(AzureInstance.class);
    when(mockAzureInstance.isIdleAfterIdleTimeout()).thenReturn(true);
    when(mockAgentInstances.find(elasticAgentId)).thenReturn(mockAzureInstance);
    DateTime currentDateTimeInUTC = DateTime.now();

    when(mockClock.now()).thenReturn(currentDateTimeInUTC);

    GoPluginApiResponse response = executor.execute();

    verify(mockAgentInstances, never()).addTag(pluginSettings, elasticAgentId, LAST_JOB_RUN_TAG_KEY,
        String.valueOf(currentDateTimeInUTC.toInstant().getMillis()));
    verify(mockAgentInstances, never()).removeTag(pluginSettings, elasticAgentId, JOB_IDENTIFIER_TAG_KEY);

    InOrder inOrder = inOrder(mockPluginRequest, mockAgentInstances);
    inOrder.verify(mockPluginRequest).getPluginSettings();
    inOrder.verify(mockPluginRequest).disableAgents(agentsArgumentCaptor.capture());
    inOrder.verify(mockAgentInstances).terminate(elasticAgentId, pluginSettings);
    inOrder.verify(mockPluginRequest).deleteAgents(agentsArgumentCaptor.capture());

    List<Agent> agentsToDisabled = agentsArgumentCaptor.getValue();
    assertEquals(1, agentsToDisabled.size());
    assertEquals(elasticAgentId, agentsToDisabled.get(0).elasticAgentId());

    List<Agent> agentsToDelete = agentsArgumentCaptor.getValue();
    assertEquals(agentsToDisabled, agentsToDelete);

    assertEquals(200, response.responseCode());
    assertTrue(response.responseBody().isEmpty());
  }
}
