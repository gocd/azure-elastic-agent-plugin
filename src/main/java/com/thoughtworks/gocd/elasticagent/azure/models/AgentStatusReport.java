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

package com.thoughtworks.gocd.elasticagent.azure.models;

import com.microsoft.azure.management.compute.ImageReference;
import com.thoughtworks.gocd.elasticagent.azure.AzureInstance;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

@Getter
@EqualsAndHashCode
public class AgentStatusReport {

  private final Platform platform;
  private final String elasticAgentId;
  private final DateTime createdAt;
  private final String status;
  private final String image;
  private final String hostName;
  private final String size;
  private final String os;
  private final String environment;
  private final String diskSize;
  private final String resourceGroup;
  private final String nic;
  private final AzureInstance.JobState state;
  private final String customScriptExecutionLogs;

  public AgentStatusReport(AzureInstance agentInstance, String customScriptExecutionLogs) {
    this.elasticAgentId = agentInstance.getName();
    this.createdAt = agentInstance.getCreatedAt();
    this.status = getStatus(agentInstance);
    this.image = getImage(agentInstance.getImageReference());
    this.hostName = agentInstance.getHostName();
    this.size = agentInstance.getSize();
    this.os = agentInstance.getOs();
    this.diskSize = String.format("%d GB", agentInstance.getDiskSize());
    this.environment = agentInstance.getEnvironment();
    this.resourceGroup = agentInstance.getResourceGroupName();
    this.nic = agentInstance.getPrimaryNetworkInterface();
    this.platform = agentInstance.getPlatform();
    this.state = agentInstance.getJobState();
    this.customScriptExecutionLogs = customScriptExecutionLogs;
  }

  private String getStatus(AzureInstance agentInstance) {
    return String.format("ProvisioningState/%s %s", agentInstance.getProvisioningState(), agentInstance.getPowerState());
  }

  public String getCreatedAt() {
    return DateTimeFormat.forPattern("MMM dd, yyyy hh:mm:ss a z").print(createdAt);
  }

  private String getImage(ImageReference imageReference) {
    return StringUtils.isEmpty(imageReference.id()) ? new ImageURN(imageReference).toString() : imageReference.id();
  }
}
