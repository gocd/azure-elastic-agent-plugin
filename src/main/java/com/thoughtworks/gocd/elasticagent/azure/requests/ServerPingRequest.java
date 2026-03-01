/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.thoughtworks.gocd.elasticagent.azure.requests;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.gocd.elasticagent.azure.AzureAgentInstances;
import com.thoughtworks.gocd.elasticagent.azure.ClusterProfileProperties;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.executors.ServerPingRequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.service.ServerHealthMessagingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author joestr
 */
public class ServerPingRequest {

  private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  private List<ClusterProfileProperties> allClusterProfileProperties = new ArrayList<>();

  public ServerPingRequest() {
  }

  public ServerPingRequest(List<Map<String, String>> allClusterProfileProperties) {
    this.allClusterProfileProperties = allClusterProfileProperties.stream()
      .map(ClusterProfileProperties::fromConfiguration)
      .collect(Collectors.toList());
  }

  public List<ClusterProfileProperties> allClusterProfileProperties() {
    return allClusterProfileProperties;
  }

  public static ServerPingRequest fromJSON(String json) {
    return GSON.fromJson(json, ServerPingRequest.class);
  }

  @Override
  public String toString() {
    return "ServerPingRequest{"
      + "allClusterProfileProperties=" + allClusterProfileProperties
      + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServerPingRequest that = (ServerPingRequest) o;
    return Objects.equals(allClusterProfileProperties, that.allClusterProfileProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(allClusterProfileProperties);
  }

  public ServerPingRequestExecutor executor(AzureAgentInstances agentInstances, PluginRequest pluginRequest, ServerHealthMessagingService serverHealthMessagingService) {
    return new ServerPingRequestExecutor(this, agentInstances, pluginRequest, serverHealthMessagingService);
  }
}
