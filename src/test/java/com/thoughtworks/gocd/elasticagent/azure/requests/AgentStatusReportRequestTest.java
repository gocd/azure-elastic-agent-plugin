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

package com.thoughtworks.gocd.elasticagent.azure.requests;

import com.google.gson.JsonObject;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifierMother;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AgentStatusReportRequestTest {

  @Test
  void shouldDeserializeFromJSON() {
    JsonObject jobIdentifierJson = JobIdentifierMother.getJson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("elastic_agent_id", "some-id");
    jsonObject.add("job_identifier", jobIdentifierJson);

    AgentStatusReportRequest agentStatusReportRequest = AgentStatusReportRequest.fromJSON(jsonObject.toString());

    AgentStatusReportRequest expected = new AgentStatusReportRequest("some-id", JobIdentifierMother.get());
    assertThat(agentStatusReportRequest, is(expected));
  }

}
