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

package com.thoughtworks.gocd.elasticagent.azure;

import com.microsoft.azure.management.compute.ImageReference;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@EqualsAndHashCode
public class AzureInstance {
  private String name;
  private String hostName;
  private String id;
  private DateTime createdAt;
  private ImageReference imageReference;
  private String size;
  private String os;
  private Integer diskSize;
  private String provisioningState;
  private String powerState;
  private String resourceGroupName;
  private String primaryNetworkInterface;
  private Map<String, String> tags;
  private Platform platform;

  public AzureInstance(String name,
                       String hostName,
                       String id,
                       DateTime createdAt,
                       ImageReference imageReference,
                       String size,
                       String os,
                       Integer diskSize,
                       String provisioningState,
                       String powerState,
                       String resourceGroupName,
                       String primaryNetworkInterface,
                       Map<String, String> tags, Platform platform) {
    this.name = name;
    this.hostName = hostName;
    this.id = id;
    this.createdAt = createdAt;
    this.imageReference = imageReference;
    this.size = size;
    this.os = os;
    this.diskSize = diskSize;
    this.provisioningState = provisioningState;
    this.powerState = powerState;
    this.resourceGroupName = resourceGroupName;
    this.primaryNetworkInterface = primaryNetworkInterface;
    this.tags = tags;
    this.platform = platform;
  }

  public Boolean jobIdentifierMatches(JobIdentifier identifier) {
    return getJobIdentifierHash().equals(identifier.hash());
  }

  public Boolean elasticProfileMatches(ElasticProfile elasticProfile){
    return getElasticProfileHash().equals(elasticProfile.hash());
  }

  public String getEnvironment() {
    return Optional.ofNullable(this.tags.get(ENVIRONMENT_TAG_KEY)).orElse("");
  }

  public boolean isAssigned() {
    return isNotBlank(tags.get(JOB_IDENTIFIER_TAG_KEY));
  }

  public JobState getJobState() {
    return isAssigned() ? JobState.Assigned : JobState.Unassigned;
  }

  public boolean canBeAssigned(ElasticProfile elasticProfile) {
    return getElasticProfileHash().equals(elasticProfile.hash()) && !isAssigned() && (neverAssigned() || !isIdleAfterIdleTimeout());
  }

  public boolean isIdleAfterIdleTimeout() {
    return Clock.DEFAULT.now().isAfter(idleSince().plusMinutes(getIdleTimeout()));
  }

  public boolean canBeTerminated() {
    return isIdleAfterIdleTimeout() && !neverAssigned();
  }

  private boolean neverAssigned() {
    return getLastJobRunTime() == null;
  }

  private String getElasticProfileHash() {
    return Optional.ofNullable(this.tags.get(ELASTIC_PROFILE_TAG_KEY)).orElse(String.valueOf(UUID.randomUUID()));
  }

  private DateTime getLastJobRunTime() {
    String lastJobRunTime = this.tags.get(LAST_JOB_RUN_TAG_KEY);
    if (lastJobRunTime != null) {
      return new DateTime(Long.valueOf(lastJobRunTime));
    }
    return null;
  }

  private String getJobIdentifierHash() {
    return Optional.ofNullable(this.tags.get(JOB_IDENTIFIER_TAG_KEY)).orElse(String.valueOf(UUID.randomUUID()));
  }

  private int getIdleTimeout(){
    String idleTimeout = this.tags.get(IDLE_TIMEOUT);
    return idleTimeout != null ? Integer.valueOf(idleTimeout) : 0;
  }

  private DateTime idleSince(){
    DateTime lastJobRunTime = getLastJobRunTime();
    return lastJobRunTime !=null ? lastJobRunTime : createdAt;
  }

  public enum JobState {
    Assigned, Unassigned
  }
}
