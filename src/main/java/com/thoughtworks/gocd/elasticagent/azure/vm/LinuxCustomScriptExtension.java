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

package com.thoughtworks.gocd.elasticagent.azure.vm;

import com.thoughtworks.gocd.elasticagent.azure.DownloadUrls;

import java.util.*;

import static com.thoughtworks.gocd.elasticagent.azure.DownloadUrls.UNZIP_TAR_DOWNLOAD_URL;

public class LinuxCustomScriptExtension implements AzureVMExtension {

  private String goAgentVersion;

  private Map<String, String> installParams;

  public LinuxCustomScriptExtension(String goAgentVersion,
                                    String goServerUrl,
                                    String autoRegisterKey,
                                    String environment,
                                    String pluginId,
                                    String agentId) {
    this.goAgentVersion = goAgentVersion;
    this.installParams = new HashMap<String, String>() {{
      put("version", goAgentVersion);
      put("go_server_url", goServerUrl);
      put("autoregister_key", autoRegisterKey);
      put("environment", environment);
      put("plugin_id", pluginId);
      put("agent_id", agentId);
    }};

  }

  @Override
  public String getName() {
    return "post-provision-script";
  }

  public List<String> getFileUris() {
    List<String> files = new ArrayList<>();
    files.add(UNZIP_TAR_DOWNLOAD_URL);
    files.add(DownloadUrls.linuxGoAgent(goAgentVersion));
    return files;
  }

  public String getScript() {
    return new CustomScriptBuilder()
        .withScript("post_provision_script.template.ftlh", this.installParams)
        .base64Encoded()
        .build();
  }

  @Override
  public String getPublisher() {
    return "Microsoft.Azure.Extensions";
  }

  @Override
  public String getType() {
    return "CustomScript";
  }

  @Override
  public String getVersion() {
    return "2.0";
  }

  @Override
  public HashMap<String, Object> publicSettings() {
    return new HashMap<>();
  }

  @Override
  public HashMap<String, Object> protectedSettings() {
    HashMap<String, Object> settings = new HashMap<>();
    settings.put("fileUris", this.getFileUris());
    settings.put("script", this.getScript());
    return settings;
  }

  Map<String, String> getInstallParams() {
    return installParams;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LinuxCustomScriptExtension that = (LinuxCustomScriptExtension) o;
    return Objects.equals(goAgentVersion, that.goAgentVersion) &&
        Objects.equals(installParams, that.installParams);
  }

  @Override
  public int hashCode() {
    return Objects.hash(goAgentVersion, installParams);
  }
}
