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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.PluginSettingsNotConfiguredException;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ServerRequestFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.PluginHealthMessage;
import com.thoughtworks.gocd.elasticagent.azure.models.ServerInfo;

import java.util.Collection;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import static com.thoughtworks.gocd.elasticagent.azure.Constants.*;
import static org.apache.commons.lang3.StringUtils.isBlank;


/**
 * Instances of this class know how to send messages to the GoCD Server.
 */
public class PluginRequest {
  private final GoApplicationAccessor accessor;
  private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).excludeFieldsWithoutExposeAnnotation().create();

  public PluginRequest(GoApplicationAccessor accessor) {
    this.accessor = accessor;
  }

  public PluginSettings getPluginSettings() throws ServerRequestFailedException, PluginSettingsNotConfiguredException {
    DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_GET_PLUGIN_SETTINGS, PLUGIN_SETTINGS_PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
    GoApiResponse response = accessor.submit(request);

    if (response.responseCode() != 200) {
      throw ServerRequestFailedException.getPluginSettings(response);
    }

    if (isBlank(response.responseBody())) {
      throw new PluginSettingsNotConfiguredException();
    }

    return PluginSettings.fromJSON(response.responseBody());
  }

  public ServerInfo getServerInfo() throws ServerRequestFailedException {
    GoApiResponse response = invokeServerInfoApi(SERVER_INFO_PROCESSOR_V2_API_VERSION);

    if(response.responseCode() != 200){
      LOG.info("Falling back to V1 Server info api");
      response = invokeServerInfoApi(SERVER_INFO_PROCESSOR_V1_API_VERSION);
    }
    return returnResponse(response);
  }

  private ServerInfo returnResponse(GoApiResponse response) throws ServerRequestFailedException {
    if (response.responseCode() != 200) {
      throw ServerRequestFailedException.serverInfo(response);
    }

    return ServerInfo.fromJSON(response.responseBody());
  }

  private GoApiResponse invokeServerInfoApi(String version) {
    DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_INFO, version, PLUGIN_IDENTIFIER);
    return accessor.submit(request);
  }

  public Agents listAgents() throws ServerRequestFailedException {
    DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_LIST_AGENTS, ELASTIC_PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
    GoApiResponse response = accessor.submit(request);

    if (response.responseCode() != 200) {
      throw ServerRequestFailedException.listAgents(response);
    }

    return new Agents(Agent.fromJSONArray(response.responseBody()));
  }

  public void disableAgents(Collection<Agent> toBeDisabled) throws ServerRequestFailedException {
    if (toBeDisabled.isEmpty()) {
      return;
    }

    DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DISABLE_AGENT, ELASTIC_PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
    request.setRequestBody(Agent.toJSONArray(toBeDisabled));

    GoApiResponse response = accessor.submit(request);

    if (response.responseCode() != 200) {
      throw ServerRequestFailedException.disableAgents(response);
    }
  }

  public void deleteAgents(Collection<Agent> toBeDeleted) throws ServerRequestFailedException {
    if (toBeDeleted.isEmpty()) {
      return;
    }

    DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_SERVER_DELETE_AGENT, ELASTIC_PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
    request.setRequestBody(Agent.toJSONArray(toBeDeleted));
    GoApiResponse response = accessor.submit(request);

    if (response.responseCode() != 200) {
      throw ServerRequestFailedException.deleteAgents(response);
    }
  }

  public synchronized void sendHealthMessages(Collection<PluginHealthMessage> healthMessages) throws ServerRequestFailedException {
    DefaultGoApiRequest request = new DefaultGoApiRequest(Constants.REQUEST_ADD_SERVER_HEALTH_MESSAGES, SERVER_INFO_PROCESSOR_V1_API_VERSION, PLUGIN_IDENTIFIER);
    request.setRequestBody(GSON.toJson(healthMessages));
    GoApiResponse response = accessor.submit(request);

    if (response.responseCode() != 200) {
      throw ServerRequestFailedException.sendHealthMessages(response);
    }
  }
}
