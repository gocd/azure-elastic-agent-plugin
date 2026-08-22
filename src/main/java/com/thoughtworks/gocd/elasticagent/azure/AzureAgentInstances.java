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

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.models.*;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class AzureAgentInstances implements AgentInstances<AzureInstance> {

  public static final Period INSTANCE_CLEANUP_INTERVAL = Period.minutes(10);
  private final ConcurrentHashMap<String, AzureInstance> instances = new ConcurrentHashMap<>();

  private boolean refreshed;
  private Clock clock = Clock.DEFAULT;
  private AzureInstanceManager azureInstanceManager;
  private GoCDAzureClientFactory clientFactory;
  private DateTime lastCleanup;

  public AzureAgentInstances(AzureInstanceManager azureInstanceManager, GoCDAzureClientFactory clientFactory) {
    this.azureInstanceManager = azureInstanceManager;
    this.clientFactory = clientFactory;
  }

  public AzureAgentInstances(AzureInstanceManager azureInstanceManager, Clock clock, GoCDAzureClientFactory clientFactory) {
    this(azureInstanceManager, clientFactory);
    this.clock = clock;
  }

  @Override
  public AzureInstance create(CreateAgentRequest request, PluginSettings settings, ServerInfo serverInfo) throws Exception {
    final AzureInstance instance = find(request.jobIdentifier());
    if (instance != null) {
      LOG.info(MessageFormat.format("Task is already scheduled on instance {0}.", instance.getName()));
      return instance;
    }
    AzureInstance instanceByElasticProfile = findAvailableInstance(request.getClusterProfileProperties());
    if (instanceByElasticProfile != null) {
      LOG.info(MessageFormat.format("Instance {0} provisioned already with the same elastic profile.", instanceByElasticProfile.getName()));
      return instanceByElasticProfile;
    }

    GoCDAzureClient goCDAzureClient = clientFactory.initialize(settings);
    AzureInstance azureInstance = azureInstanceManager.create(goCDAzureClient, request, settings, serverInfo);
    register(azureInstance);
    return azureInstance;
  }

  @Override
  public void terminate(String agentId, PluginSettings settings) throws Exception {
    GoCDAzureClient goCDAzureClient = clientFactory.initialize(settings);
    Optional.ofNullable(instances.get(agentId)).ifPresent(azureInstance -> {
      instances.remove(agentId);
      azureInstanceManager.terminate(goCDAzureClient, azureInstance);
    });
  }

  @Override
  public AzureInstance addTag(PluginSettings settings, String agentId, String tagName, String tagValue) throws IOException {
    LOG.info("Adding Tag {} to Agent {} with value {}", tagName, agentId, tagValue);
    GoCDAzureClient goCDAzureClient = clientFactory.initialize(settings);
    Optional.ofNullable(instances.get(agentId))
      .ifPresent(instance -> {
        AzureInstance instanceWithTags = azureInstanceManager.addTag(goCDAzureClient, instance, tagName, tagValue);
        register(instanceWithTags);
      });
    return instances.get(agentId);
  }

  @Override
  public void removeTag(PluginSettings settings, String agentId, String tagName) throws IOException {
    LOG.info("Removing Tag {} on Agent {}", tagName, agentId);
    GoCDAzureClient goCDAzureClient = clientFactory.initialize(settings);
    Optional.ofNullable(instances.get(agentId))
      .ifPresent(instance -> {
        AzureInstance instanceWithoutTag = azureInstanceManager.removeTag(goCDAzureClient, instance, tagName);
        register(instanceWithoutTag);
      });
  }

  @Override
  public void terminateUnregisteredInstances(PluginSettings settings, Agents agents) throws Exception {
    List<AzureInstance> instancesToTerminate = unregisteredAfterTimeout(settings.getAutoRegisterPeriod(), agents);
    if (!instancesToTerminate.isEmpty()) {
      String instanceNames = String.join(",", instancesToTerminate.stream()
        .map(AzureInstance::getName)
        .collect(Collectors.toCollection(ArrayList::new)));
      LOG.warn("Terminating instances that did not register " + instanceNames);
      for (AzureInstance instance : instancesToTerminate) {
        terminate(instance.getName(), settings);
      }
    }
  }

  @Override
  public Agents instancesToBeDisabled(PluginSettings settings, Agents agents) {
    ArrayList<Agent> agentsToBeDisabled = new ArrayList<>();
    agents.agents().forEach(agent -> {
      AzureInstance instance = instances.get(agent.elasticAgentId());
      if (instance != null) {
        if (isCreatedAfterAutoRegisterTimeout(settings, instance) || instance.canBeTerminated()) {
          agentsToBeDisabled.add(agent);
        }
      }
    });
    return new Agents(agentsToBeDisabled);
  }

  private boolean isCreatedAfterAutoRegisterTimeout(PluginSettings settings, AzureInstance instance) {
    return isAfterTimeoutPeriod(settings.getAutoRegisterPeriod(), instance.getCreatedAt());
  }

  @Override
  public void refreshAll(ClusterProfileProperties clusterProfileProperties) throws Exception {
    GoCDAzureClient goCDAzureClient = clientFactory.initialize(clusterProfileProperties);
    if (!refreshed) {
      List<AzureInstance> instances = azureInstanceManager.listInstances(goCDAzureClient, clusterProfileProperties.getResourceGroup());
      instances.forEach(instance -> register(instance));
      refreshed = true;
    }
  }

  public void terminateProvisionFailedInstances(PluginSettings pluginSettings, ServerInfo serverInfo) throws Exception {
    if (lastCleanup == null || clock.now().isAfter(lastCleanup.plus(INSTANCE_CLEANUP_INTERVAL))) {
      lastCleanup = clock.now();
      GoCDAzureClient goCDAzureClient = clientFactory.initialize(pluginSettings);
      azureInstanceManager.terminateProvisionFailedVms(goCDAzureClient, serverInfo.getServerId());
    }
  }

  @Override
  public AzureInstance find(String agentId) {
    return instances.get(agentId);
  }

  @Override
  public AzureInstance find(JobIdentifier jobIdentifier) {
    return instances.values().stream().filter((instance) -> instance.jobIdentifierMatches(jobIdentifier)).findFirst().orElse(null);
  }

  public AzureInstance findAvailableInstance(ClusterProfileProperties elasticProfile) {
    return instances.values().stream().filter((instance) -> instance.canBeAssigned(elasticProfile)).findFirst().orElse(null);
  }

  @Override
  public StatusReport getStatusReport(PluginSettings pluginSettings) {
    return new StatusReport(new ArrayList<>(this.instances.values()), Util.pluginVersion());
  }

  @Override
  public AgentStatusReport getAgentStatusReport(PluginSettings pluginSettings, AzureInstance agentInstance) {
    return new AgentStatusReport(agentInstance, azureInstanceManager.getExecutionLogs(agentInstance.getName()));
  }

  private void register(AzureInstance instance) {
    instances.put(instance.getName(), instance);
  }

  private List<AzureInstance> unregisteredAfterTimeout(Period autoregisterTimeout, Agents knownAgents) {
    ArrayList<AzureInstance> unregisteredInstances = new ArrayList<>();
    instances.values().forEach((azureInstance) -> {
      if (!knownAgents.containsAgentWithId(azureInstance.getName()) && isAfterTimeoutPeriod(autoregisterTimeout, azureInstance.getCreatedAt())) {
        unregisteredInstances.add(azureInstance);
      }
    });
    return unregisteredInstances;
  }

  private boolean isAfterTimeoutPeriod(Period period, DateTime timestamp) {
    return clock.now().isAfter(timestamp.plus(period));
  }
}
