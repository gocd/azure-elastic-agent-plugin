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

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;

import java.util.Collections;

public interface Constants {
  String PLUGIN_ID = Util.pluginId();

  // The type of this extension
  String EXTENSION_TYPE = "elastic-agent";

  // The extension point API version that this plugin understands
  String ELASTIC_PROCESSOR_API_VERSION = "1.0";
  String PLUGIN_SETTINGS_PROCESSOR_API_VERSION = "1.0";
  String SERVER_INFO_PROCESSOR_V1_API_VERSION = "1.0";
  String SERVER_INFO_PROCESSOR_V2_API_VERSION = "2.0";
  String EXTENSION_API_VERSION = "4.0";

  // the identifier of this plugin
  GoPluginIdentifier PLUGIN_IDENTIFIER = new GoPluginIdentifier(EXTENSION_TYPE, Collections.singletonList(EXTENSION_API_VERSION));

  // requests that the plugin makes to the server
  String REQUEST_SERVER_PREFIX = "go.processor";
  String REQUEST_SERVER_DISABLE_AGENT = REQUEST_SERVER_PREFIX + ".elastic-agents.disable-agents";
  String REQUEST_SERVER_DELETE_AGENT = REQUEST_SERVER_PREFIX + ".elastic-agents.delete-agents";
  String REQUEST_SERVER_GET_PLUGIN_SETTINGS = REQUEST_SERVER_PREFIX + ".plugin-settings.get";
  String REQUEST_SERVER_LIST_AGENTS = REQUEST_SERVER_PREFIX + ".elastic-agents.list-agents";
  String REQUEST_SERVER_INFO = REQUEST_SERVER_PREFIX + ".server-info.get";
  String REQUEST_ADD_SERVER_HEALTH_MESSAGES = REQUEST_SERVER_PREFIX + ".server-health.add-messages";

  String SUPPORTED_GO_SERVER_VERSION= "19.2.0-8641";

}
