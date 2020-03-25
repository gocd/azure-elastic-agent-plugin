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

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.*;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifierMother;
import com.thoughtworks.gocd.elasticagent.azure.requests.ShouldAssignWorkRequest;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;
import com.thoughtworks.gocd.elasticagent.azure.vm.VMTags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class ShouldAssignWorkRequestExecutorTest extends BaseTest {

  @Mock
  private AzureAgentInstances agentInstances;

  private final String environment = "production";
  private final JobIdentifier jobIdentifier = JobIdentifierMother.get();
  private String agentId = "agent-id";

  @Mock
  private AzureInstance instance;

  @Mock
  private PluginSettings pluginSettings;

  @Mock
  private ElasticProfile elasticProfile;

  @Mock
  private ServerHealthMessagingService serverHealthMessagingService;


  @BeforeEach
  void setUp() {
    initMocks(this);
    when(agentInstances.find(agentId)).thenReturn(instance);
  }

  @Test
  void shouldAssignWorkToInstanceWhichIsAssignableToTheElasticProfileAndClearHealthMessages() throws Exception {
    ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(agentId, null, null, null), environment, jobIdentifier, elasticProfile);
    when(instance.canBeAssigned(elasticProfile)).thenReturn(true);
    when(instance.getName()).thenReturn(agentId);

    GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, pluginSettings, serverHealthMessagingService).execute();

    assertThat(response.responseCode(), is(200));
    assertThat(response.responseBody(), is("true"));
    verify(agentInstances).addTag(pluginSettings, agentId, VMTags.JOB_IDENTIFIER_TAG_KEY, jobIdentifier.hash());
    verify(serverHealthMessagingService).clearHealthMessage(jobIdentifier.getJobRepresentation());
  }

  @Test
  void shouldNotAssignWorkIfInstanceIsNotFound() {
    ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent("unknown-name", null, null, null), environment, jobIdentifier, null);
    GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, pluginSettings, serverHealthMessagingService).execute();
    assertThat(response.responseCode(), is(200));
    assertThat(response.responseBody(), is("false"));
  }

  @Test
  void shouldNotAssignWorkToInstanceWhichIsNotAssignableToTheElasticProfile() throws Exception {
    ShouldAssignWorkRequest request = new ShouldAssignWorkRequest(new Agent(agentId, null, null, null), environment, jobIdentifier, elasticProfile);
    when(instance.canBeAssigned(elasticProfile)).thenReturn(false);
    when(instance.getName()).thenReturn(agentId);

    GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(request, agentInstances, pluginSettings, serverHealthMessagingService).execute();

    assertThat(response.responseCode(), is(200));
    assertThat(response.responseBody(), is("false"));
    verify(agentInstances, never()).addTag(any(), anyString(), anyString(), anyString());
  }
}
