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
import com.thoughtworks.gocd.elasticagent.azure.exceptions.PluginSettingsNotConfiguredException;
import com.thoughtworks.gocd.elasticagent.azure.models.StatusReport;
import com.thoughtworks.gocd.elasticagent.azure.utils.TemplateReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class StatusReportExecutorTest {

  @Mock
  private PluginRequest pluginRequest;

  @Mock
  private PluginSettings pluginSettings;

  @Mock
  private TemplateReader templateReader;

  @Mock
  private AzureAgentInstances agentInstances;

  @Captor
  private ArgumentCaptor<Map<String, String>> templateParamCaptor;

  @BeforeEach
  void setUp() {
    openMocks(this);
  }

  @Test
  void shouldGetStatusReport() throws Exception {
    List<AzureInstance> instances = new ArrayList<>();
    StatusReport statusReport = new StatusReport(instances, "0.1.2");

    when(pluginRequest.getPluginSettings()).thenReturn(pluginSettings);
    when(agentInstances.getStatusReport(pluginSettings)).thenReturn(statusReport);
    when(templateReader.read(StatusReportExecutor.STATUS_REPORT_TEMPLATE, statusReport)).thenReturn("statusReportView");
    StatusReportExecutor statusReportExecutor = new StatusReportExecutor(pluginRequest, agentInstances, templateReader);

    GoPluginApiResponse goPluginApiResponse = statusReportExecutor.execute();

    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("view", "statusReportView");
    assertThat(goPluginApiResponse.responseCode(), is(200));
    JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    verify(agentInstances).refreshAll(pluginRequest);
  }

  @Test
  void shouldGetErrorStatusReportWhenPluginNotConfigured() throws Exception {
    when(pluginRequest.getPluginSettings()).thenThrow(PluginSettingsNotConfiguredException.class);
    when(templateReader.read(eq(StatusReportExecutor.ERROR_TEMPLATE), templateParamCaptor.capture())).thenReturn("errorReportView");
    StatusReportExecutor statusReportExecutor = new StatusReportExecutor(pluginRequest, agentInstances, templateReader);

    GoPluginApiResponse goPluginApiResponse = statusReportExecutor.execute();

    JsonObject expectedResponseBody = new JsonObject();
    expectedResponseBody.addProperty("view", "errorReportView");
    assertThat(goPluginApiResponse.responseCode(), is(200));
    JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    verify(agentInstances).refreshAll(pluginRequest);

    Map<String, String> params = templateParamCaptor.getValue();
    assertEquals("Unable to generate status report", params.get("message"));
    assertEquals("Azure Plugin not configured", params.get("description"));
  }
}
