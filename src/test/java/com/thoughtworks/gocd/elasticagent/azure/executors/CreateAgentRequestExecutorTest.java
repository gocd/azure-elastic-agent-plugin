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

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ProvisionFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifierMother;
import com.thoughtworks.gocd.elasticagent.azure.models.PluginHealthMessage;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import com.thoughtworks.gocd.elasticagent.azure.requests.RequestFingerprintCache;
import com.thoughtworks.gocd.elasticagent.azure.requests.RequestFingerprintCache.SupplierThrowingException;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;
import org.joda.time.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class CreateAgentRequestExecutorTest {

  @Captor
  ArgumentCaptor<SupplierThrowingException> lambdaCaptor;

  @Captor
  private ArgumentCaptor<PluginHealthMessage> healthMessagesCaptor;

  @Mock(answer = RETURNS_DEEP_STUBS)
  PluginRequest pluginRequest;

  @Mock
  RequestFingerprintCache requestFingerprintCache;

  @Mock
  AzureAgentInstances agentInstances;

  @Mock
  private ServerHealthMessagingService serverHealthMessagingService;

  @BeforeEach
  void setUp() {
    initMocks(this);
  }

  @Test
  void shouldAskAzureToCreateAnAgentAndRegisterRequest() throws Exception {
    JobIdentifier jobId = JobIdentifierMother.get();
    CreateAgentRequest request = new CreateAgentRequest("key", null, null, jobId);
    Period timeoutPeriod = Period.minutes(2);
    PluginSettings settings = mock(PluginSettings.class);
    ServerInfo serverInfo = mock(ServerInfo.class);

    when(settings.getAutoRegisterPeriod()).thenReturn(timeoutPeriod);
    when(pluginRequest.getPluginSettings()).thenReturn(settings);
    when(pluginRequest.getServerInfo()).thenReturn(serverInfo);

    new CreateAgentRequestExecutor(request, agentInstances, pluginRequest, requestFingerprintCache, serverHealthMessagingService).execute();

    verify(requestFingerprintCache, times(1))
        .getOrExecute(eq(jobId.hash()), eq(timeoutPeriod), lambdaCaptor.capture());

    SupplierThrowingException lambda = lambdaCaptor.getValue();
    lambda.get();
    verify(agentInstances, times(1)).create(request, settings, serverInfo);
    verify(serverHealthMessagingService).clearHealthMessage(jobId.getJobRepresentation());
    serverHealthMessagingService.clearHealthMessage(request.jobIdentifier().getJobRepresentation());
  }

  @Test
  void shouldSendPluginHealthMessageOnProvisionFailure() throws Exception {
    PluginRequest pluginRequest = mock(PluginRequest.class, RETURNS_DEEP_STUBS);
    CreateAgentRequest request = mock(CreateAgentRequest.class, RETURNS_DEEP_STUBS);
    JobIdentifier jobIdentifier = JobIdentifierMother.get();
    ProvisionFailedException provisionFailedException = new ProvisionFailedException(jobIdentifier, "provision failed");
    doThrow(provisionFailedException).when(requestFingerprintCache).getOrExecute(any(), any(), any());

    GoPluginApiResponse apiResponse = new CreateAgentRequestExecutor(request, agentInstances, pluginRequest, requestFingerprintCache, serverHealthMessagingService).execute();

    assertEquals(500, apiResponse.responseCode());
    assertEquals("provision failed", apiResponse.responseBody());
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(serverHealthMessagingService).sendHealthMessage(stringArgumentCaptor.capture(), healthMessagesCaptor.capture());

    PluginHealthMessage message = healthMessagesCaptor.getValue();
    String actualKey = stringArgumentCaptor.getValue();

    assertEquals(jobIdentifier.getJobRepresentation(), actualKey);
    String expectedJSON = "{\"message\": \"provision failed\", \"type\": \"error\"}";
    JSONAssert.assertEquals(expectedJSON, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(message), true);
  }
}
