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

package com.thoughtworks.gocd.elasticagent.azure.validations;

import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class SubnetNameValidationTest {

  @BeforeEach
  void setUp() {
    openMocks(this);
  }

  @Mock
  private PluginSettings settings;

  @Test
  void shouldReturnErrorsWhenSubnetNameNotInPluginSettingsSubnet() {
    Map<String, String> properties = Collections.singletonMap(ElasticProfile.SUBNET_NAME, "subnet2");
    when(settings.getSubnetNames()).thenReturn(ArrayUtils.toArray("subnet4", "subnet5"));
    Map<String, String> errors = new SubnetNameValidation().run(properties, settings, null);

    Assertions.assertEquals(1, errors.size());
    Assertions.assertEquals("Subnet name must be one of: subnet4, subnet5", errors.get(ElasticProfile.SUBNET_NAME));
  }

  @Test
  void shouldNotReturnErrorsWhenSubnetNameIsInPluginSettingsSubnet() {
    Map<String, String> properties = Collections.singletonMap(ElasticProfile.SUBNET_NAME, "subnet4");
    when(settings.getSubnetNames()).thenReturn(ArrayUtils.toArray("subnet4", "subnet5"));
    Map<String, String> errors = new SubnetNameValidation().run(properties, settings, null);

    Assertions.assertEquals(0, errors.size());
  }
}
