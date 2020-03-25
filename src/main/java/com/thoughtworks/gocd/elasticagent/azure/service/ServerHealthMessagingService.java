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

package com.thoughtworks.gocd.elasticagent.azure.service;

import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ServerRequestFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.PluginHealthMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;

public class ServerHealthMessagingService {
  private PluginRequest pluginRequest;
  private Map<String, PluginHealthMessage> currentHealthMessages;

  public ServerHealthMessagingService(PluginRequest pluginRequest) {
    this.pluginRequest = pluginRequest;
    currentHealthMessages = new ConcurrentHashMap<>();
  }

  public void sendHealthMessage(String key, PluginHealthMessage healthMessage) throws ServerRequestFailedException {
    List<PluginHealthMessage> messages = new ArrayList<>(currentHealthMessages.values());
    messages.add(healthMessage);
    pluginRequest.sendHealthMessages(messages);
    currentHealthMessages.put(key, healthMessage);
  }

  public void clearHealthMessage(String key) throws ServerRequestFailedException {
    if (currentHealthMessages.containsKey(key)) {
      LOG.info("Clearing message with key: {} from server health", key);
      pluginRequest.sendHealthMessages(filteredHealthMessagesByKey(key));
      currentHealthMessages.remove(key);
    }
  }

  public void clearExpiredHealthMessages() throws ServerRequestFailedException {
    int initialSize = currentHealthMessages.size();
    currentHealthMessages.entrySet().removeIf(messageEntry -> messageEntry.getValue().isExpired());
    if (currentHealthMessages.size() != initialSize) {
      pluginRequest.sendHealthMessages(new ArrayList<>(currentHealthMessages.values()));
    }
  }

  private List<PluginHealthMessage> filteredHealthMessagesByKey(String key) {
    ArrayList<PluginHealthMessage> filteredMessages = new ArrayList<>();
    currentHealthMessages.entrySet().forEach(messageEntry -> {
      if (!messageEntry.getKey().equals(key)) {
        filteredMessages.add(messageEntry.getValue());
      }
    });
    return filteredMessages;
  }
}
