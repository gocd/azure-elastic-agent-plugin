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

import com.thoughtworks.gocd.elasticagent.azure.BaseTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.gocd.elasticagent.azure.models.StatusReport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusReportTest extends BaseTest {

  @Test
  void testShouldSetProperties() {
    StatusReport statusReport = new StatusReport(Arrays.asList(createAzureInstance("instance1", Platform.WINDOWS), createAzureInstance("instance2", Platform.LINUX)), "12");

    List<Pair<String, String>> properties = statusReport.getProperties();
    assertEquals(3, properties.size());
    assertEquals(TOTAL_NUMBER_OF_VIRTUAL_MACHINES, properties.get(0).getKey());
    assertEquals("2", properties.get(0).getValue());
    assertEquals(TOTAL_NUMBER_OF_LINUX_VIRTUAL_MACHINES, properties.get(1).getKey());
    assertEquals("1", properties.get(1).getValue());
    assertEquals(TOTAL_NUMBER_OF_WINDOWS_VIRTUAL_MACHINES, properties.get(2).getKey());
    assertEquals("1", properties.get(2).getValue());

    assertEquals("12", statusReport.getVersion());
    assertEquals(2, statusReport.getAgentStatusReports().size());

    AgentStatusReport agentStatusReport = statusReport.getAgentStatusReports().get(0);
    assertEquals("nic", agentStatusReport.getNic());
    assertEquals("instance1", agentStatusReport.getElasticAgentId());

    AgentStatusReport agentStatusReport2 = statusReport.getAgentStatusReports().get(1);
    assertEquals("instance2", agentStatusReport2.getElasticAgentId());
  }
}
