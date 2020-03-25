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

import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithCreate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class AzureVMExtensionTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  WithCreate mockVM;

  @BeforeEach
  void setUp() {
    initMocks(this);
  }

  @Test
  void shouldAddCustomScriptExtensionToVM() throws Exception {
    AzureVMExtension extension = new AzureVMExtension() {
      @Override
      public String getName() {
        return "test-extension";
      }

      @Override
      public String getPublisher() {
        return "test-publisher";
      }

      @Override
      public String getType() {
        return "extension-type";
      }

      @Override
      public String getVersion() {
        return "v1.0";
      }

      @Override
      public HashMap<String, Object> publicSettings() {
        return new HashMap<String, Object>() {{
          put("key3", "value3");
        }};
      }

      @Override
      public HashMap<String, Object> protectedSettings() throws Exception {
        return new HashMap<String, Object>() {{
          put("key1", "value1");
          put("key2", "value2");
        }};
      }
    };

    WithCreate vmWithExtension = mock(WithCreate.class);
    when(mockVM.defineNewExtension("test-extension")
        .withPublisher("test-publisher")
        .withType("extension-type")
        .withVersion("v1.0")
        .withMinorVersionAutoUpgrade()
        .withProtectedSettings(new HashMap<String, Object>() {{
          put("key1", "value1");
          put("key2", "value2");
        }})
        .withPublicSettings(new HashMap<String, Object>() {{
          put("key3", "value3");
        }})
        .attach())
        .thenReturn(vmWithExtension);

    WithCreate actualVM = extension.addTo(mockVM);

    assertEquals(vmWithExtension, actualVM);
  }
}
