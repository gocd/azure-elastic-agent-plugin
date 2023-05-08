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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import static com.thoughtworks.gocd.elasticagent.azure.Request.REQUEST_AGENT_STATUS_REPORT;
import static com.thoughtworks.gocd.elasticagent.azure.Request.REQUEST_CAPABILITIES;
import static com.thoughtworks.gocd.elasticagent.azure.Request.REQUEST_GET_CLUSTER_PROFILE_METADATA;
import static com.thoughtworks.gocd.elasticagent.azure.Request.REQUEST_GET_ELASTIC_AGENT_PROFILE_METADATA;
import static com.thoughtworks.gocd.elasticagent.azure.Request.REQUEST_GET_ELASTIC_AGENT_PROFILE_VIEW;
import static com.thoughtworks.gocd.elasticagent.azure.Request.REQUEST_JOB_COMPLETION;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.PluginSettingsNotConfiguredException;
import com.thoughtworks.gocd.elasticagent.azure.executors.*;
import com.thoughtworks.gocd.elasticagent.azure.requests.*;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;
import com.thoughtworks.gocd.elasticagent.azure.utils.TemplateReader;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Extension
public class AzurePlugin implements GoPlugin {

  public static final Logger LOG = Logger.getLoggerFor(AzurePlugin.class);

  private PluginRequest pluginRequest;
  private AzureAgentInstances agentInstances;
  private RequestFingerprintCache requestFingerprintCache;
  private GoCDAzureClientFactory clientFactory;
  private ServerHealthMessagingService serverHealthMessagingService;

  @Override
  public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
    pluginRequest = new PluginRequest(accessor);
    clientFactory = new GoCDAzureClientFactory();
    agentInstances = new AzureAgentInstances(new AzureInstanceManager(new AzureInstanceMapper()), clientFactory);
    requestFingerprintCache = new RequestFingerprintCache();
    serverHealthMessagingService = new ServerHealthMessagingService(pluginRequest);
  }

  @Override
  public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
    try {
      switch (Request.fromString(request.requestName())) {
        case REQUEST_GET_ICON:
          return new GetPluginIconExecutor()
            .execute();
        case REQUEST_SHOULD_ASSIGN_WORK:
          refreshInstances();
          return ShouldAssignWorkRequest.fromJSON(request.requestBody())
            .executor(agentInstances, pluginRequest.getPluginSettings(), serverHealthMessagingService)
            .execute();
        case REQUEST_CREATE_AGENT:
          refreshInstances();
          return CreateAgentRequest.fromJSON(request.requestBody())
            .executor(agentInstances, pluginRequest, requestFingerprintCache, serverHealthMessagingService)
            .execute();
        case REQUEST_SERVER_PING:
          refreshInstances();
          return new ServerPingRequestExecutor(agentInstances, pluginRequest, serverHealthMessagingService)
            .execute();
        case REQUEST_GET_ELASTIC_AGENT_PROFILE_METADATA:
          return new GetElasticAgentProfileMetadataExecutor()
            .execute();
        case REQUEST_GET_ELASTIC_AGENT_PROFILE_VIEW:
          return new GetElasticAgentProfileViewExecutor()
            .execute();
        case REQUEST_JOB_COMPLETION:
          refreshInstances();
          return JobCompletionRequest.fromJSON(request.requestBody())
            .executor(agentInstances, pluginRequest).execute();
        case REQUEST_CAPABILITIES:
          return new GetCapabilitiesExecutor()
            .execute();
        case REQUEST_AGENT_STATUS_REPORT:
          refreshInstances();
          return AgentStatusReportRequest.fromJSON(request.requestBody())
            .executor(pluginRequest, agentInstances, TemplateReader.instance()).execute();
        case REQUEST_GET_CLUSTER_PROFILE_METADATA:
          return new GetClusterProfileMetadataExecutor()
            .execute();
        case REQUEST_GET_CLUSTER_PROFILE_VIEW:
          return new GetClusterProfileViewRequestExecutor()
            .execute();
        case REQUEST_VALIDATE_CLUSTER_PROFILE:
          return ClusterProfileValidateRequest.fromJSON(request.requestBody())
            .executor(pluginRequest, clientFactory).execute();
        case REQUEST_PLUGIN_STATUS_REPORT:
          return new StatusReportExecutor(pluginRequest, agentInstances, TemplateReader.instance())
            .execute();
        default:
          throw new UnhandledRequestTypeException(request.requestName());
      }
    } catch (PluginSettingsNotConfiguredException e) {
      LOG.warn("Request {} failed: {}", request.requestName(), e.getMessage());
      return DefaultGoPluginApiResponse.success(e.getMessage());
    } catch (UnhandledRequestTypeException e) {
      LOG.warn("UnhandledRequest {} failed: {}", request.requestName(), e.getMessage());
      throw e;
    } catch (Exception e) {
      LOG.error("Something went wrong for request {} with exception: {}", request.requestName(), e.getMessage());
      LOG.error(ExceptionUtils.getStackTrace(e));
      throw new RuntimeException(e);
    }
  }

  private void refreshInstances() throws Exception {
    agentInstances.refreshAll(pluginRequest);
  }

  @Override
  public GoPluginIdentifier pluginIdentifier() {
    return Constants.PLUGIN_IDENTIFIER;
  }

}
