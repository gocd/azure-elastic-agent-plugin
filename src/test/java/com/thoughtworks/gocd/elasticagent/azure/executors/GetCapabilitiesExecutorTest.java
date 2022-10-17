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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GetCapabilitiesExecutorTest {

  @Test
  void shouldReturnResponse() throws Exception {
    GoPluginApiResponse response = new GetCapabilitiesExecutor().execute();

    assertThat(response.responseCode(), is(200));
    JSONObject expected = new JSONObject().put("supports_status_report", true);
    expected.put("supports_agent_status_report", true);
    JSONAssert.assertEquals(expected, new JSONObject(response.responseBody()), true);
  }
}
