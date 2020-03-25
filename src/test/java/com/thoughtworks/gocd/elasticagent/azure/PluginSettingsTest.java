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

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.joda.time.Period;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginSettingsTest {
  @Test
  void shouldDeserializeFromJSON() {
    PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
        "\"client_id\": \"bob\", " +
        "\"secret\": \"p@ssw0rd\", " +
        "\"domain\": \"domain-123\" " +
        "}");

    assertThat(pluginSettings.getClientId(), is("bob"));
    assertThat(pluginSettings.getSecret(), is("p@ssw0rd"));
    assertThat(pluginSettings.getDomain(), is("domain-123"));
  }

  @Test
  void shouldReturnIdleTimeoutPeriodIfConfigured() {
    PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
        "\"client_id\": \"bob\", " +
        "\"secret\": \"p@ssw0rd\", " +
        "\"domain\": \"domain-123\", " +
        "\"idle_timeout\": \"12\" " +
        "}");

    assertEquals(new Period().withMinutes(12), pluginSettings.getIdleTimeoutPeriod());
  }

  @Test
  void shouldReturnDefaultIdleTimeoutPeriodIfNotConfigured() {
    PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
        "\"client_id\": \"bob\", " +
        "\"secret\": \"p@ssw0rd\", " +
        "\"domain\": \"domain-123\" " +
        "}");

    assertEquals(new Period().withMinutes(0), pluginSettings.getIdleTimeoutPeriod());
  }

  @Test
  void shouldReturnRegionForRegionName() {
    PluginSettings pluginSettings = PluginSettings.fromJSON("{ \"region_name\": \"East US\" }");

    assertEquals(Region.US_EAST, pluginSettings.getRegion());
  }

  @Test
  void shouldReturnSubnetNamesSeparatedByComma() {
    PluginSettings pluginSettings = PluginSettings.fromJSON("{ \"subnet\": \"default, first\" }");

    String[] subnetNames = pluginSettings.getSubnetNames();
    assertEquals(2, subnetNames.length);
    assertEquals("default", subnetNames[0]);
    assertEquals("first", subnetNames[1]);
  }

  @Test
  void shouldReturnSingleSubnetName() {
    PluginSettings pluginSettings = PluginSettings.fromJSON("{ \"subnet\": \"default \" }");

    String[] subnetNames = pluginSettings.getSubnetNames();
    assertEquals(1, subnetNames.length);
    assertEquals("default", subnetNames[0]);

    assertEquals("default", pluginSettings.getRandomSubnet());
  }
}
