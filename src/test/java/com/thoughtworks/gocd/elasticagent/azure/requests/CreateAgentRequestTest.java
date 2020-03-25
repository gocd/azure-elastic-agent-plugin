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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CreateAgentRequestTest {

  @Test
  void shouldDeserializeFromJSON() throws Exception {
    String json = "{\n" +
        "  \"auto_register_key\": \"secret-key\",\n" +
        "  \"properties\": {\n" +
        "    \"vm_size\": \"Standard_d2\",\n" +
        "    \"vm_image_urn\": \"Canonical:UbuntuServer:14.04.4-LTS\"\n" +
        "  },\n" +
        "  \"environment\": \"prod\"\n" +
        "}";

    CreateAgentRequest request = CreateAgentRequest.fromJSON(json);
    assertThat(request.autoRegisterKey(), equalTo("secret-key"));
    assertThat(request.elasticProfile().getImageReference().publisher(), equalTo("Canonical"));
    assertThat(request.elasticProfile().getImageReference().offer(), equalTo("UbuntuServer"));
    assertThat(request.elasticProfile().getImageReference().sku(), equalTo("14.04.4-LTS"));
    assertThat(request.elasticProfile().getImageReference().version(), equalTo("latest"));
    assertThat(request.elasticProfile().getVmSize(), equalTo("Standard_d2"));
  }
}
