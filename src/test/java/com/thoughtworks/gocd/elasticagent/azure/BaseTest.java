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

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.Collections;

public abstract class BaseTest {

  protected PluginSettings createPluginSettings() {
    return new PluginSettings("serverUrl",
        "2",
        "15",
        "domain",
        "clientId",
        "secret",
        Period.minutes(2),
        "networkId",
        "subnet",
        "nsg-123",
        "resourceGroup",
        "sshKey",
        Region.INDIA_SOUTH.name(),
        "username",
        "adminUserName",
        "@dminP@$$w0rd");
  }

  protected AzureInstance createAzureInstance(String name) {
    return createAzureInstance(name, Platform.LINUX);
  }

  protected AzureInstance createAzureInstance(String name, Platform platform) {
    return new AzureInstance(
        name,
        "hostname",
        Util.uniqueString(name),
        DateTime.now(),
        KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS.imageReference(),
        "Standard_D3",
        "Linux",
        100,
        "Succeeded",
        "Running",
        "resource-group",
        "nic",
        Collections.emptyMap(),
        platform);
  }
}
