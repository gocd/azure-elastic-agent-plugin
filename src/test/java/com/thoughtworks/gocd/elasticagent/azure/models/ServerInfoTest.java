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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServerInfoTest {

  @Test
  void shouldReturnServerVersionFromServerInfoAttributes() {
    String serverInfoJson = "{\"go_version\": \"19.1.0\", \"dist_version\": \"9999\", \"git_revision\": \"3483947387483\"}";
    ServerInfo info = ServerInfo.fromJSON(serverInfoJson);

    assertEquals("19.1.0-9999", info.getServerVersion());
  }

  @Test
  void shouldReturnNullServerVersionIfAttributesAreNotSet() {
    String serverInfoJson = "{\"server_id\": \"server-123\"}";
    ServerInfo info = ServerInfo.fromJSON(serverInfoJson);

    assertNull(info.getServerVersion());
  }

}
