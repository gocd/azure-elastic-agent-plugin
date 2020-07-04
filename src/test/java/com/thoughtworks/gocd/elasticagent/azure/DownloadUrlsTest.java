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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DownloadUrlsTest {

  @Test
  void testLinuxGoAgentDownloadUrlShouldFetchDefaultURLForVersionNotPublished() {
    assertEquals("https://download.gocd.org/binaries/20.5.0-11820/generic/go-agent-20.5.0-11820.zip", DownloadUrls.linuxGoAgent("not valid"));
  }

  @Test
  void testWindowsGoAgentDownloadUrlShouldFetchDefaultURLForVersionNotPublished() {
    assertEquals("https://download.gocd.org/binaries/20.5.0-11820/win/go-agent-20.5.0-11820-jre-64bit-setup.exe", DownloadUrls.windowsGoAgent("19.xyz"));
  }

  @Test
  void testWindowsGoAgentDownloadUrlShouldFetchURLForPublishedVersion() {
    assertEquals("https://download.gocd.org/binaries/19.6.0-9515/win/go-agent-19.6.0-9515-jre-64bit-setup.exe", DownloadUrls.windowsGoAgent("19.6.0-9515"));
  }

  @Test
  void testLinuxGoAgentDownloadUrlShouldFetchURLForPublishedVersion() {
    assertEquals("https://download.gocd.org/binaries/19.6.0-9515/generic/go-agent-19.6.0-9515.zip", DownloadUrls.linuxGoAgent("19.6.0-9515"));
  }
}
