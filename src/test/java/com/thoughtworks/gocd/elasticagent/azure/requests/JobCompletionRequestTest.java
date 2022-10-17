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

package com.thoughtworks.gocd.elasticagent.azure.requests;

import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JobCompletionRequestTest {
  @Test
  void shouldDeserializeFromJSON() {
    String json = "{\n" +
        "  \"elastic_agent_id\": \"ea1\",\n" +
        "  \"job_identifier\": {\n" +
        "    \"pipeline_name\": \"test-pipeline\",\n" +
        "    \"pipeline_counter\": 1,\n" +
        "    \"pipeline_label\": \"Test Pipeline\",\n" +
        "    \"stage_name\": \"test-stage\",\n" +
        "    \"stage_counter\": \"1\",\n" +
        "    \"job_name\": \"test-job\",\n" +
        "    \"job_id\": 100\n" +
        "  }\n" +
        "}";
    JobCompletionRequest request = JobCompletionRequest.fromJSON(json);
    JobIdentifier expectedJobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
    JobIdentifier actualJobIdentifier = request.jobIdentifier();

    assertEquals(expectedJobIdentifier, actualJobIdentifier);
    assertEquals("ea1", request.getElasticAgentId());
  }
}
