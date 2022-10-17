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

import com.microsoft.azure.management.compute.ImageReference;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.models.*;
import com.thoughtworks.gocd.elasticagent.azure.requests.CreateAgentRequest;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AzureAgentInstancesTest extends BaseTest {

  @Mock
  private AzureInstanceManager mockAzureInstanceManager;

  @Mock
  private GoCDAzureClientFactory mockClientFactory;

  @Mock
  private GoCDAzureClient mockGoCDAzureClient;
  private AzureAgentInstances instances;

  @Mock
  private Clock mockClock;

  @BeforeEach
  void setup() {
    openMocks(this);
    instances = new AzureAgentInstances(mockAzureInstanceManager, mockClock, mockClientFactory);
  }

  @Test
  void shouldCreateAzureInstance() throws Exception {

    PluginSettings settings = createPluginSettings();
    CreateAgentRequest request = new CreateAgentRequest();
    AzureInstance expectedInstance = mock(AzureInstance.class);
    ServerInfo serverInfo = mock(ServerInfo.class);
    when(serverInfo.getServerId()).thenReturn("server_id");
    when(expectedInstance.getName()).thenReturn("Agent-new");
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    when(mockAzureInstanceManager.create(mockGoCDAzureClient, request, settings, serverInfo)).thenReturn(expectedInstance);

    AzureInstance azureInstance = instances.create(request, settings, serverInfo);

    verify(mockAzureInstanceManager).create(mockGoCDAzureClient, request, settings, serverInfo);
    assertEquals(expectedInstance, instances.find("Agent-new"));
    assertEquals(expectedInstance, azureInstance);
  }

  @Test
  void shouldReturnExistingInstanceIfAlreadyCreatedForJob() throws Exception {
    PluginSettings settings = createPluginSettings();
    ServerInfo serverInfo = mock(ServerInfo.class);
    CreateAgentRequest request = getCreateAgentRequestForJob(new JobIdentifier(2L));
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    AzureInstance expectedInstance = insertMockAzureInstance(instances, settings, "instanceName", request, serverInfo);

    AzureInstance azureInstance = instances.create(request, settings, serverInfo);

    verify(mockAzureInstanceManager, times(1)).create(mockGoCDAzureClient, request, settings, serverInfo);
    assertEquals(expectedInstance, azureInstance);
  }

  @Test
  void shouldTerminateAzureInstance() throws Exception {
    PluginSettings settings = createPluginSettings();
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    when(request.jobIdentifier()).thenReturn(new JobIdentifier(2L));
    AzureInstance instance = insertMockAzureInstance(instances, settings, "instanceName", request, mock(ServerInfo.class));

    instances.terminate("instanceName", settings);

    verify(mockAzureInstanceManager).terminate(mockGoCDAzureClient, instance);
    assertNull(instances.find("instanceName"));
  }

  @Test
  void terminateShouldDoNothingWhenInstanceDoesNotExist() throws Exception {
    PluginSettings settings = createPluginSettings();
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    instances.terminate("instanceName", settings);

    verify(mockAzureInstanceManager, never()).terminate(any(), any());
  }

  @Test
  void testRefreshAllShouldFetchAllInstances() throws Exception {
    PluginSettings settings = createPluginSettings();
    PluginRequest request = mock(PluginRequest.class, RETURNS_DEEP_STUBS);
    AzureInstance instance1 = mock(AzureInstance.class);
    AzureInstance instance2 = mock(AzureInstance.class);
    List<AzureInstance> newInstances = asList(instance1, instance2);
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    when(request.getPluginSettings()).thenReturn(settings);
    when(request.getServerInfo().getServerId()).thenReturn("server_id");
    when(instance1.getName()).thenReturn("instance1");
    when(instance2.getName()).thenReturn("instance2");
    when(mockAzureInstanceManager.listInstances(mockGoCDAzureClient, "server_id")).thenReturn(newInstances);

    instances.refreshAll(request);

    verify(mockAzureInstanceManager, times(1)).listInstances(mockGoCDAzureClient, "server_id");
    assertEquals(instance1, instances.find("instance1"));
    assertEquals(instance2, instances.find("instance2"));
  }

  @Test
  void testRefreshAllShouldFetchAllInstancesOnlyOnce() throws Exception {
    PluginSettings settings = createPluginSettings();
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    JobIdentifier identifier = new JobIdentifier(2L);
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    when(request.jobIdentifier()).thenReturn(identifier);
    AzureInstance expectedInstance = insertMockAzureInstance(instances, settings, "instanceName", request, mock(ServerInfo.class));

    assertEquals(expectedInstance, instances.find(identifier));
  }

  @Test
  void testFindReturnsNullIfNoAzureInstance() {
    assertNull(instances.find(new JobIdentifier()));
  }

  @Test
  void testFindInstanceByJobIdentifier() throws Exception {
    PluginSettings settings = createPluginSettings();
    PluginRequest request = mock(PluginRequest.class, RETURNS_DEEP_STUBS);
    when(request.getServerInfo().getServerId()).thenReturn("server_id");
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    when(request.getPluginSettings()).thenReturn(settings);
    when(mockAzureInstanceManager.listInstances(mockGoCDAzureClient, "server_id")).thenReturn(new ArrayList<>());

    instances.refreshAll(request);
    instances.refreshAll(request);

    verify(mockAzureInstanceManager, times(1)).listInstances(mockGoCDAzureClient, "server_id");
  }

  @Test
  void shouldFindAssignableInstanceByElasticProfileBeforeCreatingAnInstance() throws Exception {
    PluginSettings settings = createPluginSettings();
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient).thenReturn(mockGoCDAzureClient);
    ElasticProfile profile = new ElasticProfile();
    CreateAgentRequest request1 = getCreateAgentRequestForJob(new JobIdentifier(1L));
    JobIdentifier jobId2 = new JobIdentifier(2L);
    CreateAgentRequest request2 = getCreateAgentRequestForJob(jobId2);
    AzureInstance expectedInstance = mock(AzureInstance.class);
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    when(expectedInstance.getName()).thenReturn("instance-with-ep");
    when(expectedInstance.elasticProfileMatches(profile)).thenReturn(true);

    when(request1.elasticProfile()).thenReturn(profile);
    when(request2.elasticProfile()).thenReturn(profile);
    when(mockAzureInstanceManager.create(mockGoCDAzureClient, request1, settings, mockServerInfo))
        .thenReturn(expectedInstance);

    AzureInstance instanceForRequest1 = instances.create(request1, settings, mockServerInfo);
    when(instanceForRequest1.canBeAssigned(profile)).thenReturn(true);

    AzureInstance instanceForRequest2 = instances.create(request2, settings, mockServerInfo);

    verify(mockAzureInstanceManager).create(mockGoCDAzureClient, request1, settings, mockServerInfo);
    assertEquals(expectedInstance, instanceForRequest1);
    assertEquals(expectedInstance, instanceForRequest2);
  }

  @Test
  void testFindAvailableInstanceByElasticProfile() throws Exception {
    PluginSettings settings = createPluginSettings();
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    ServerInfo serverInfo = mock(ServerInfo.class);
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    ElasticProfile elasticProfile = new ElasticProfile("Size-1", "urn-1", "image-123", "", Platform.WINDOWS, "Standard_LRS", "", "50", "");
    when(request.elasticProfile()).thenReturn(elasticProfile);
    AzureInstance expectedAvailableInstance = insertMockAzureInstance(instances, settings, "instance-with-ep-job-unassigned", request, serverInfo);
    insertMockAzureInstance(instances, settings, "instance-with-someother-ep", getCreateAgentRequestForJob(new JobIdentifier(1L)), serverInfo);
    when(expectedAvailableInstance.canBeAssigned(elasticProfile)).thenReturn(true);

    assertEquals(expectedAvailableInstance, instances.findAvailableInstance(elasticProfile));
    assertNull(instances.findAvailableInstance(new ElasticProfile()));
  }

  @Test
  void testTerminateUnregisteredInstanceShouldTerminateInstancesCreatedAfterAutoRegisterTimeout() throws Exception {
    PluginSettings settings = createPluginSettings();
    Clock clock = mock(Clock.class);
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    when(request.jobIdentifier()).thenReturn(new JobIdentifier(1L)).thenReturn(new JobIdentifier(2L));
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    AzureAgentInstances instances = new AzureAgentInstances(mockAzureInstanceManager, clock, mockClientFactory);
    AzureInstance instance1 = insertMockAzureInstance(instances, settings, "instanceName1", request, mock(ServerInfo.class));
    AzureInstance instance2 = insertMockAzureInstance(instances, settings, "instanceName2", request, mock(ServerInfo.class));

    DateTime firstInstanceCreatedTime = new DateTime(2018, 12, 1, 10, 0);
    when(instance1.getCreatedAt()).thenReturn(firstInstanceCreatedTime);
    when(instance2.getCreatedAt()).thenReturn(firstInstanceCreatedTime.plus(Period.minutes(2)));
    when(clock.now()).thenReturn(firstInstanceCreatedTime.plus(Period.minutes(3)));

    instances.terminateUnregisteredInstances(settings, new Agents(new ArrayList<>()));

    verify(mockAzureInstanceManager, times(1)).terminate(mockGoCDAzureClient, instance1);
  }

  @Test
  void testTerminateUnregisteredInstanceShouldNotTerminateInstancesCreatedAfterAutoRegisterTimeoutButPresentInKnownAgents() throws Exception {
    PluginSettings settings = createPluginSettings();
    Clock clock = mock(Clock.class);
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    when(request.jobIdentifier()).thenReturn(new JobIdentifier(1L)).thenReturn(new JobIdentifier(2L));
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    AzureAgentInstances instances = new AzureAgentInstances(mockAzureInstanceManager, clock, mockClientFactory);
    AzureInstance instance1 = insertMockAzureInstance(instances, settings, "instanceName1", request, mockServerInfo);
    AzureInstance instance2 = insertMockAzureInstance(instances, settings, "instanceName2", request, mockServerInfo);

    DateTime firstInstanceCreatedTime = new DateTime(2018, 12, 1, 10, 0);
    when(instance1.getCreatedAt()).thenReturn(firstInstanceCreatedTime);
    when(instance2.getCreatedAt()).thenReturn(firstInstanceCreatedTime.plus(Period.minutes(2)));
    when(clock.now()).thenReturn(firstInstanceCreatedTime.plus(Period.minutes(3)));

    Agent agent1 = getAgentWithAgentId("instanceName1");
    instances.terminateUnregisteredInstances(settings, new Agents(singletonList(agent1)));

    verify(mockAzureInstanceManager, never()).terminate(any(), any());
  }

  @Test
  void testTerminateUnregisteredInstancesShouldNotTerminateAnyInstancesWhenNoneRegistered() throws Exception {
    PluginSettings settings = createPluginSettings();
    Clock clock = mock(Clock.class);
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    when(request.jobIdentifier()).thenReturn(new JobIdentifier(1L)).thenReturn(new JobIdentifier(2L));
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    AzureAgentInstances instances = new AzureAgentInstances(mockAzureInstanceManager, clock, mockClientFactory);

    instances.terminateUnregisteredInstances(settings, new Agents(new ArrayList<>()));

    verify(mockAzureInstanceManager, never()).terminate(any(), any());
  }

  @Test
  void shouldGetAgentStatusReport() {
    PluginSettings settings = createPluginSettings();
    DateTime createdAt = DateTime.now();
    AzureInstance agentInstance = mock(AzureInstance.class);
    when(agentInstance.getName()).thenReturn("VM-uuid");
    when(agentInstance.getProvisioningState()).thenReturn("Succeeded");
    when(agentInstance.getPowerState()).thenReturn("PowerState/running");
    when(agentInstance.getCreatedAt()).thenReturn(createdAt);
    ImageReference imageReference = new ImageReference()
        .withPublisher("MS")
        .withOffer("offer")
        .withSku("sku")
        .withVersion("v10");
    when(agentInstance.getImageReference()).thenReturn(imageReference);
    when(agentInstance.getHostName()).thenReturn("vm-host-1234");
    when(agentInstance.getSize()).thenReturn("vm-size");
    when(agentInstance.getOs()).thenReturn("Linux");
    when(agentInstance.getDiskSize()).thenReturn(2);
    when(agentInstance.getEnvironment()).thenReturn("Testing");
    when(agentInstance.getResourceGroupName()).thenReturn("resource-group");
    when(agentInstance.getPrimaryNetworkInterface()).thenReturn("Test-VNet");

    AgentStatusReport agentStatusReport = instances.getAgentStatusReport(settings, agentInstance);

    assertEquals("VM-uuid", agentStatusReport.getElasticAgentId());
    assertEquals("vm-host-1234", agentStatusReport.getHostName());
    assertEquals(DateTimeFormat.forPattern("MMM dd, yyyy hh:mm:ss a z").print(createdAt), agentStatusReport.getCreatedAt());
    assertEquals("ProvisioningState/Succeeded PowerState/running", agentStatusReport.getStatus());
    assertEquals("MS:offer:sku:v10", agentStatusReport.getImage());
    assertEquals("vm-size", agentStatusReport.getSize());
    assertEquals("Linux", agentStatusReport.getOs());
    assertEquals("2 GB", agentStatusReport.getDiskSize());
    assertEquals("Testing", agentStatusReport.getEnvironment());
    assertEquals("resource-group", agentStatusReport.getResourceGroup());
    assertEquals("Test-VNet", agentStatusReport.getNic());

  }

  @Test
  void shouldAddTagToAzureInstance() throws Exception {
    PluginSettings settings = createPluginSettings();
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    AzureInstance instance = insertMockAzureInstance(instances, settings, "agent-123", request, mock(ServerInfo.class));
    AzureInstance mockInstanceWithTag = mock(AzureInstance.class);
    when(mockInstanceWithTag.getName()).thenReturn("agent-123");
    when(mockAzureInstanceManager.addTag(eq(mockGoCDAzureClient), eq(instance), anyString(), anyString())).thenReturn(mockInstanceWithTag);

    instances.addTag(settings, "agent-123", "tag-1", "value-1");

    verify(mockAzureInstanceManager).addTag(mockGoCDAzureClient, instance, "tag-1", "value-1");
    assertEquals(mockInstanceWithTag, instances.find("agent-123"));
  }

  @Test
  void shouldRemoveTagFromAzureInstance() throws Exception {
    PluginSettings settings = createPluginSettings();
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    AzureInstance instance = insertMockAzureInstance(instances, settings, "agent-123", request, mock(ServerInfo.class));
    AzureInstance mockInstanceWithoutTag = mock(AzureInstance.class);
    when(mockInstanceWithoutTag.getName()).thenReturn("agent-123");
    when(mockAzureInstanceManager.removeTag(eq(mockGoCDAzureClient), eq(instance), anyString())).thenReturn(mockInstanceWithoutTag);

    instances.removeTag(settings, "agent-123", "tag-1");

    verify(mockAzureInstanceManager).removeTag(mockGoCDAzureClient, instance, "tag-1");
    assertEquals(mockInstanceWithoutTag, instances.find("agent-123"));
  }

  @Test
  void shouldNotAddTagIfTheAgentIsNoLongerRegistered() throws IOException {
    PluginSettings settings = createPluginSettings();

    instances.addTag(settings, "agent-123", "tag-1", "value-1");

    verify(mockAzureInstanceManager, never()).addTag(eq(mockGoCDAzureClient), any(AzureInstance.class), eq("tag-1"), eq("value-1"));
  }

  @Test
  void shouldFetchInstancesCreatedAfterAutoRegisterTimeoutAsToBeDisabled() throws Exception {
    PluginSettings settings = mock(PluginSettings.class);
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    when(settings.getAutoRegisterPeriod()).thenReturn(Period.minutes(5));
    Agent agent1 = getAgentWithAgentId("instanceName1");
    Agent agent2 = getAgentWithAgentId("instanceName2");
    Clock clock = mock(Clock.class);

    Agents agents = new Agents(asList(agent1, agent2));
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);
    AzureAgentInstances instances = new AzureAgentInstances(mockAzureInstanceManager, clock, mockClientFactory);
    AzureInstance instance1 = insertMockAzureInstance(instances, settings, "instanceName1", getCreateAgentRequestForJob(new JobIdentifier(1L)), mockServerInfo);
    AzureInstance instance2 = insertMockAzureInstance(instances, settings, "instanceName2", getCreateAgentRequestForJob(new JobIdentifier(2L)), mockServerInfo);

    DateTime now = DateTime.now();
    DateTime firstInstanceCreatedTime = now.minusMinutes(6);
    DateTime secondInstanceCreatedTime = now.minusMinutes(2);
    when(instance1.getCreatedAt()).thenReturn(firstInstanceCreatedTime);
    when(instance1.isIdleAfterIdleTimeout()).thenReturn(true);

    when(instance2.getCreatedAt()).thenReturn(secondInstanceCreatedTime);
    when(instance2.isIdleAfterIdleTimeout()).thenReturn(false);

    when(clock.now()).thenReturn(now);

    Agents actualInstances = instances.instancesToBeDisabled(settings, agents);

    assertEquals(1, actualInstances.agents().size());
    Agent actualAgent = actualInstances.agents().toArray(new Agent[1])[0];
    assertEquals(agent1, actualAgent);
  }


  @Test
  void shouldReturnAgentsWhichAreIdlePostIdleTimeoutPeriodToBeDisabled() throws Exception {
    DateTime now = DateTime.now();
    PluginSettings settings = mock(PluginSettings.class);
    when(settings.getAutoRegisterPeriod()).thenReturn(Period.minutes(6));
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    Clock mockClock = mock(Clock.class);
    when(mockClock.now()).thenReturn(now);
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    Agent agentIdleJustNow = getAgentWithAgentId("recently-idle-agent");
    Agent agentIdleForALongTime = getAgentWithAgentId("idle-for-a-long-time");

    AzureAgentInstances instances = new AzureAgentInstances(mockAzureInstanceManager, mockClock, mockClientFactory);

    AzureInstance recentlyIdleInstance = insertMockAzureInstance(instances, settings, "recently-idle-agent", getCreateAgentRequestForJob(new JobIdentifier(2L)), mockServerInfo);
    when(recentlyIdleInstance.isIdleAfterIdleTimeout()).thenReturn(false);
    when(recentlyIdleInstance.getCreatedAt()).thenReturn(now.minusMinutes(5));

    AzureInstance idleInstanceForALongTime = insertMockAzureInstance(instances, settings, "idle-for-a-long-time", getCreateAgentRequestForJob(new JobIdentifier(3L)), mockServerInfo);
    when(idleInstanceForALongTime.isIdleAfterIdleTimeout()).thenReturn(true);
    when(idleInstanceForALongTime.getCreatedAt()).thenReturn(now.minusMinutes(15));


    Agents agents = new Agents(asList(agentIdleJustNow, agentIdleForALongTime));

    Agents idleAgentsPostIdleTimeout = instances.instancesToBeDisabled(settings, agents);
    assertEquals(new LinkedHashSet<>(asList("idle-for-a-long-time")), idleAgentsPostIdleTimeout.agentIds());
  }

  @Test
  void shouldTerminateFailedProvisionedInstances() throws Exception {
    PluginSettings settings = mock(PluginSettings.class);
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    when(mockServerInfo.getServerId()).thenReturn("serverId");
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    instances.terminateProvisionFailedInstances(settings, mockServerInfo);

    verify(mockAzureInstanceManager).terminateProvisionFailedVms(mockGoCDAzureClient, "serverId");
  }

  @Test
  void shouldNotTerminateFailedProvisionedInstancesBefore10MinutesAfterProvisioning() throws Exception {
    PluginSettings settings = mock(PluginSettings.class);
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    when(mockServerInfo.getServerId()).thenReturn("serverId");
    when(mockClock.now()).thenReturn(DateTime.now().minusMinutes(5)).thenReturn(DateTime.now());
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    instances.terminateProvisionFailedInstances(settings, mockServerInfo);
    instances.terminateProvisionFailedInstances(settings, mockServerInfo);

    verify(mockAzureInstanceManager, times(1)).terminateProvisionFailedVms(mockGoCDAzureClient, "serverId");
  }

  @Test
  void shouldTerminateFailedProvisionedInstances10MinutesAfterProvisioning() throws Exception {
    PluginSettings settings = mock(PluginSettings.class);
    ServerInfo mockServerInfo = mock(ServerInfo.class);
    when(mockServerInfo.getServerId()).thenReturn("serverId");
    when(mockClock.now()).thenReturn(DateTime.now().minusMinutes(11)).thenReturn(DateTime.now());
    when(mockClientFactory.initialize(settings)).thenReturn(mockGoCDAzureClient);

    instances.terminateProvisionFailedInstances(settings, mockServerInfo);
    instances.terminateProvisionFailedInstances(settings, mockServerInfo);

    verify(mockAzureInstanceManager, times(2)).terminateProvisionFailedVms(mockGoCDAzureClient, "serverId");
  }

  private Agent getAgentWithAgentId(String agentId) {
    Agent runningAgent = mock(Agent.class);
    when(runningAgent.elasticAgentId()).thenReturn(agentId);
    return runningAgent;
  }

  private CreateAgentRequest getCreateAgentRequestForJob(JobIdentifier jobIdentifier) {
    CreateAgentRequest request = mock(CreateAgentRequest.class);
    when(request.jobIdentifier()).thenReturn(jobIdentifier);
    return request;
  }

  private AzureInstance insertMockAzureInstance(AzureAgentInstances instances, PluginSettings settings, String instanceName, CreateAgentRequest request, ServerInfo serverInfo) throws Exception {
    AzureInstance instance = mock(AzureInstance.class);
    when(instance.getName()).thenReturn(instanceName);
    JobIdentifier identifier = request.jobIdentifier();
    when(instance.jobIdentifierMatches(identifier)).thenReturn(true);
    when(mockAzureInstanceManager.create(mockGoCDAzureClient, request, settings, serverInfo)).thenReturn(instance);
    instances.create(request, settings, serverInfo);
    return instance;
  }

}
