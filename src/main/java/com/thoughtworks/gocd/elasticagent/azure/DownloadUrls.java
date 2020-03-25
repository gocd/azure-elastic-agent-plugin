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

package com.thoughtworks.gocd.elasticagent.azure;

import com.thoughtworks.gocd.elasticagent.azure.utils.HttpUtil;

import static com.thoughtworks.gocd.elasticagent.azure.Constants.SUPPORTED_GO_SERVER_VERSION;

public class DownloadUrls {
  public static final String UNZIP_TAR_DOWNLOAD_URL = "https://oss.oracle.com/el4/unzip/unzip.tar";
  private static final String GO_AGENT_WINDOWS_DOWNLOAD_URL_FORMAT = "https://download.gocd.org/binaries/%s/win/go-agent-%s-jre-64bit-setup.exe";
  public static String GO_AGENT_LINUX_DOWNLOAD_URL_FORMAT = "https://download.gocd.org/binaries/%s/generic/go-agent-%s.zip";

  public static String linuxGoAgent(String goAgentVersion) {
    String latestGoAgentUrl = String.format(GO_AGENT_LINUX_DOWNLOAD_URL_FORMAT, goAgentVersion, goAgentVersion);
    String defaultGoAgentUrl = String.format(GO_AGENT_LINUX_DOWNLOAD_URL_FORMAT, SUPPORTED_GO_SERVER_VERSION, SUPPORTED_GO_SERVER_VERSION);
    return HttpUtil.isValidUrl(latestGoAgentUrl) ? latestGoAgentUrl : defaultGoAgentUrl;
  }

  public static String windowsGoAgent(String goAgentVersion) {
    String latestGoAgentUrl = String.format(GO_AGENT_WINDOWS_DOWNLOAD_URL_FORMAT, goAgentVersion, goAgentVersion);
    String defaultGoAgentUrl = String.format(GO_AGENT_WINDOWS_DOWNLOAD_URL_FORMAT, SUPPORTED_GO_SERVER_VERSION, SUPPORTED_GO_SERVER_VERSION);
    return HttpUtil.isValidUrl(latestGoAgentUrl) ? latestGoAgentUrl : defaultGoAgentUrl;
  }
}
