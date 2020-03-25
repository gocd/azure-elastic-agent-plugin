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
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.AzureInstance;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.models.AgentStatusReport;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.requests.AgentStatusReportRequest;
import com.thoughtworks.gocd.elasticagent.azure.utils.TemplateReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class AgentStatusReportExecutorTest {

  @Mock
  private PluginRequest pluginRequest;
  @Mock
  private PluginSettings pluginSettings;
  @Mock
  private AzureAgentInstances agentInstances;
  @Mock
  private TemplateReader templateReader;

  @BeforeEach
  void setUp() {
    initMocks(this);
  }

  @Test
  void shouldGetAgentStatusReportWithElasticAgentId() throws Exception {
    String agentId = "elastic-agent-id";
    JobIdentifier jobId = new JobIdentifier(1L);
    AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(agentId, jobId);
    AgentStatusReport agentStatusReport = mock(AgentStatusReport.class);
    when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    AzureInstance agentInstance = mock(AzureInstance.class);
    when(agentInstances.find(agentId)).thenReturn(agentInstance);
    when(agentInstances.getAgentStatusReport(pluginSettings, agentInstance)).thenReturn(agentStatusReport);
    when(templateReader.read("agent-status-report.template.ftlh", agentStatusReport)).thenReturn("agentStatusReportView");

    GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, agentInstances, templateReader)
        .execute();

    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("view", "agentStatusReportView");
    assertThat(goPluginApiResponse.responseCode(), is(200));
    JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
  }

  @Test
  void shouldGetAgentStatusReportWithJobIdentifier() throws Exception {
    JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "label", "stage1", "1", "job", 1L);
    AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(null, jobIdentifier);
    AgentStatusReport agentStatusReport = mock(AgentStatusReport.class);
    when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    AzureInstance instance = mock(AzureInstance.class);
    when(agentInstances.find(jobIdentifier)).thenReturn(instance);
    when(agentInstances.getAgentStatusReport(pluginSettings, instance)).thenReturn(agentStatusReport);
    when(templateReader.read("agent-status-report.template.ftlh", agentStatusReport)).thenReturn("agentStatusReportView");

    GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, agentInstances, templateReader)
        .execute();

    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("view", "agentStatusReportView");
    assertThat(goPluginApiResponse.responseCode(), is(200));
    JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
  }

  @Test
  void shouldRenderContainerNotFoundAgentStatusReportViewWhenNoContainerIsRunningForProvidedJobIdentifier() throws Exception {
    JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "label", "stage1", "1", "job", 1L);

    AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(null, jobIdentifier);

    when(agentInstances.find(jobIdentifier)).thenReturn(null);
    when(templateReader.read(eq("not-running.template.ftlh"), anyMap())).thenReturn("errorView");

    GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, agentInstances, templateReader)
        .execute();

    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("view", "errorView");
    assertThat(goPluginApiResponse.responseCode(), is(200));
    JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
  }

  @Test
  void shouldRenderContainerNotFoundAgentStatusReportViewWhenNoContainerIsRunningForProvidedElasticAgentId() throws Exception {
    String elasticAgentId = "elastic-agent-id";
    AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest(elasticAgentId, null);

    when(agentInstances.find(elasticAgentId)).thenReturn(null);
    when(templateReader.read(eq("not-running.template.ftlh"), anyMap())).thenReturn("errorView");

    GoPluginApiResponse goPluginApiResponse = new AgentStatusReportExecutor(agentStatusReportRequest, pluginRequest, agentInstances, templateReader)
        .execute();

    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("view", "errorView");
    assertThat(goPluginApiResponse.responseCode(), is(200));
    JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
  }
}
