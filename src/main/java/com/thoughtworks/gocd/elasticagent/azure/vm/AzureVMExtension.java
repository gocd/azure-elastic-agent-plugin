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

import java.util.HashMap;

public interface AzureVMExtension {

  String getName();

  String getPublisher();

  String getType();

  String getVersion();

  HashMap<String, Object> publicSettings() throws Exception;

  HashMap<String, Object> protectedSettings() throws Exception;

  default WithCreate addTo(WithCreate vm) throws Exception {
    vm = vm.defineNewExtension(this.getName())
        .withPublisher(this.getPublisher())
        .withType(this.getType())
        .withVersion(this.getVersion())
        .withMinorVersionAutoUpgrade()
        .withProtectedSettings(this.protectedSettings())
        .withPublicSettings(this.publicSettings())
        .attach();
    return vm;
  }

}
