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

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AgentTest {

  @Test
  void shouldSerializeToJSON() throws Exception {
    Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
    String agentsJSON = Agent.toJSONArray(Arrays.asList(agent));

    JSONAssert.assertEquals("[{\"agent_id\":\"eeb9e0eb-1f12-4366-a5a5-59011810273b\",\"agent_state\":\"Building\",\"build_state\":\"Cancelled\",\"config_state\":\"Disabled\"}]", agentsJSON, true);
  }

  @Test
  void shouldDeserializeFromJSON() throws Exception {
    List<Agent> agents = Agent.fromJSONArray("[{\"agent_id\":\"eeb9e0eb-1f12-4366-a5a5-59011810273b\",\"agent_state\":\"Building\",\"build_state\":\"Cancelled\",\"config_state\":\"Disabled\"}]");
    assertThat(agents, hasSize(1));

    Agent agent = agents.get(0);

    assertThat(agent.elasticAgentId(), is("eeb9e0eb-1f12-4366-a5a5-59011810273b"));
    assertThat(agent.agentState(), is(Agent.AgentState.Building));
    assertThat(agent.buildState(), is(Agent.BuildState.Cancelled));
    assertThat(agent.configState(), is(Agent.ConfigState.Disabled));
  }

  @Test
  void agentsWithSameAttributesShouldBeEqual() throws Exception {
    Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
    Agent agent2 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

    assertTrue(agent1.equals(agent2));
  }

  @Test
  void agentShouldEqualItself() throws Exception {
    Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

    assertTrue(agent.equals(agent));
  }

  @Test
  void agentShouldNotEqualAnotherAgentWithDifferentAttributes() throws Exception {
    Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

    assertFalse(agent.equals(new Agent()));
  }

  @Test
  void agentsWithSameAttributesShareSameHashCode() throws Exception {
    Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
    Agent agent2 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);

    assertThat(agent1.hashCode(), equalTo(agent2.hashCode()));
  }

  @Test
  void agentsWithDifferentAttributesDoNotShareSameHashCode() throws Exception {
    Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", Agent.AgentState.Building, Agent.BuildState.Cancelled, Agent.ConfigState.Disabled);
    Agent agent2 = new Agent();

    assertThat(agent1.hashCode(), not(equalTo(agent2.hashCode())));
  }
}
