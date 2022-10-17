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

package com.thoughtworks.gocd.elasticagent.azure.models;

import com.thoughtworks.gocd.elasticagent.azure.AzureInstance;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@EqualsAndHashCode
public class StatusReport {

  public static final String TOTAL_NUMBER_OF_VIRTUAL_MACHINES = "Total number of virtual machines";
  public static final String TOTAL_NUMBER_OF_WINDOWS_VIRTUAL_MACHINES = "Windows virtual machines";
  public static final String TOTAL_NUMBER_OF_LINUX_VIRTUAL_MACHINES = "Linux virtual machines";
  private final String version;
  private final List<AgentStatusReport> agentStatusReports;

  public StatusReport(List<AzureInstance> instances, String version) {
    this.agentStatusReports = instances.stream().map(agentInstance -> new AgentStatusReport(agentInstance, "")).collect(Collectors.toCollection(ArrayList::new));
    this.version = version;
  }

  public List<Pair<String, String>> getProperties() {
    return new ArrayList<Pair<String, String>>(){{
      add(Pair.of(TOTAL_NUMBER_OF_VIRTUAL_MACHINES, Integer.toString(agentStatusReports.size())));
      add(Pair.of(TOTAL_NUMBER_OF_LINUX_VIRTUAL_MACHINES, Integer.toString(linuxInstanceCount())));
      add(Pair.of(TOTAL_NUMBER_OF_WINDOWS_VIRTUAL_MACHINES, Integer.toString(windowsInstanceCount())));
    }};
  }

  private int linuxInstanceCount() {
    return platformInstanceCount(Platform.LINUX);
  }

  private int windowsInstanceCount() {
    return platformInstanceCount(Platform.WINDOWS);
  }

  private int platformInstanceCount(Platform platform) {
    return (int) agentStatusReports.stream().filter(agentStatusReport -> platform.equals(agentStatusReport.getPlatform())).count();
  }

  public String getVersion() {
    return version;
  }
}
