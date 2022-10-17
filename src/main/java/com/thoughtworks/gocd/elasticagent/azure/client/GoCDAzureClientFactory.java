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

package com.thoughtworks.gocd.elasticagent.azure.client;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;

import java.io.IOException;

public class GoCDAzureClientFactory {

  public GoCDAzureClient initialize(PluginSettings settings) throws IOException {
    return initialize(settings.getClientId(), settings.getDomain(), settings.getSecret(), settings.getResourceGroup(), settings.getNetworkId());
  }

  public GoCDAzureClient initialize(String clientId, String domain, String secret, String resourceGroup, String networkID) throws IOException {
    String subscriptionID = networkID.replaceAll("^/subscriptions/([0-9a-f-]+)/.*", "$1");
    return createClient(clientId, domain, secret, resourceGroup, subscriptionID);
  }

  protected GoCDAzureClient createClient(String clientId, String domain, String secret, String resourceGroup, String subscriptionID) {
    ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(clientId,
        domain,
        secret,
        AzureEnvironment.AZURE);

    Azure azure = Azure.configure()
        .withLogLevel(LogLevel.BASIC)
        .authenticate(credentials)
        .withSubscription(subscriptionID);
    return new GoCDAzureClient(azure, resourceGroup, new NetworkDecorator(azure));
  }
}
