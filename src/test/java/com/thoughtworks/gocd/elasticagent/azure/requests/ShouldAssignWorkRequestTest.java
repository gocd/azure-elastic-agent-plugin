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

import com.thoughtworks.gocd.elasticagent.azure.Agent;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ShouldAssignWorkRequestTest {

  @Test
  void shouldDeserializeFromJSON() throws Exception {
    String json = "{\n" +
        "  \"environment\": \"prod\",\n" +
        "  \"agent\": {\n" +
        "    \"agent_id\": \"42\",\n" +
        "    \"agent_state\": \"Idle\",\n" +
        "    \"build_state\": \"Idle\",\n" +
        "    \"config_state\": \"Enabled\"\n" +
        "  },\n" +
        "  \"properties\": {\n" +
        "    \"vm_size\": \"Standard_d2\",\n" +
        "    \"vm_image_urn\": \"Canonical:UbuntuServer:14.04.4-LTS\"\n" +
        "  }\n" +
        "}";

    ShouldAssignWorkRequest request = ShouldAssignWorkRequest.fromJSON(json);
    assertThat(request.environment(), equalTo("prod"));
    assertThat(request.agent(), equalTo(new Agent("42", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled)));
    ElasticProfile elasticProfile = request.elasticProfile();
    assertThat(elasticProfile.getVmSize(), equalTo("Standard_d2"));
    assertThat(elasticProfile.getImageReference().publisher(), equalTo("Canonical"));
    assertThat(elasticProfile.getImageReference().offer(), equalTo("UbuntuServer"));
    assertThat(elasticProfile.getImageReference().sku(), equalTo("14.04.4-LTS"));
    assertThat(elasticProfile.getImageReference().version(), equalTo("latest"));
  }
}
