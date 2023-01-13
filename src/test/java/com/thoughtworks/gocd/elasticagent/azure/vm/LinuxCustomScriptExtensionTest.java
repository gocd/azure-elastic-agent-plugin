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

package com.thoughtworks.gocd.elasticagent.azure.vm;

import com.thoughtworks.gocd.elasticagent.azure.DownloadUrls;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.gocd.elasticagent.azure.Constants.DEFAULT_GO_SERVER_VERSION;
import static com.thoughtworks.gocd.elasticagent.azure.DownloadUrls.UNZIP_TAR_DOWNLOAD_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LinuxCustomScriptExtensionTest {

  @Test
  void shouldCreateAzureCustomExtensionScriptToInstallGoAgent() {
    String goAgentVersion = "19.4.0";
    LinuxCustomScriptExtension extension = new LinuxCustomScriptExtension(goAgentVersion,
        "http://go-server/go",
        "auto-register-key",
        "test",
        "plugin-id",
        "agent-id"
    );

    List<String> expectedFileUris = Arrays.asList(UNZIP_TAR_DOWNLOAD_URL,
        String.format(DownloadUrls.GO_AGENT_LINUX_DOWNLOAD_URL_FORMAT, DEFAULT_GO_SERVER_VERSION, DEFAULT_GO_SERVER_VERSION));
    String base64EncodedScript = new CustomScriptBuilder()
        .withScript("post_provision_script.template.ftl", extension.getInstallParams())
        .base64Encoded()
        .build();

    assertEquals(expectedFileUris, extension.getFileUris());
    assertEquals(base64EncodedScript, extension.getScript());
    assertEquals("2.0", extension.getVersion());
    assertEquals("CustomScript", extension.getType());
    assertEquals("Microsoft.Azure.Extensions", extension.getPublisher());
  }

}
