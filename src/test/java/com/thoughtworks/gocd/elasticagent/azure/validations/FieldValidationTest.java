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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.executors.GetClusterProfileMetadataExecutor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldValidationTest {

  @Test
  void shouldFetchFieldErrors() {
    Map<String, String> errors = new FieldValidation().run(Collections.emptyMap(), null, null);
    assertEquals(13, errors.size());

    assertEquals("Go Server URL must not be blank.", errors.get(GO_SERVER_URL.key()));
    assertEquals("Agent auto-register Timeout (in minutes) must be a positive integer.", errors.get(AUTOREGISTER_TIMEOUT.key()));
    assertEquals("Linux User Name must not be blank.", errors.get(LINUX_USER_NAME.key()));
    assertEquals("Ssh key must not be blank.", errors.get(SSH_KEY.key()));
    assertEquals("Windows User Name must not be blank.", errors.get(WINDOWS_USER_NAME.key()));
    assertEquals("Windows Password must not be blank.", errors.get(WINDOWS_PASSWORD.key()));
    assertEquals("Domain/Tenant Id must not be blank.", errors.get(DOMAIN.key()));
    assertEquals("Client Id must not be blank.", errors.get(CLIENT_ID.key()));
    assertEquals("Secret must not be blank.", errors.get(SECRET.key()));
    assertEquals("Region Name must not be blank.", errors.get(REGION_NAME.key()));
    assertEquals("Resource Group must not be blank.", errors.get(RESOURCE_GROUP.key()));
    assertEquals("Virtual Network Id must not be blank.", errors.get(NETWORK_ID.key()));
    assertEquals("Subnet Name must not be blank.", errors.get(SUBNET_NAMES.key()));
  }
}
