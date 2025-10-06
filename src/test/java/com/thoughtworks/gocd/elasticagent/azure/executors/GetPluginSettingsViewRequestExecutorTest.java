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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.models.Field;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class GetPluginSettingsViewRequestExecutorTest {

  @Test
  void shouldRenderTheTemplateInJSON() throws Exception {
    GoPluginApiResponse response = new GetClusterProfileViewRequestExecutor().execute();
    assertThat(response.responseCode(), is(200));
    Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
    }.getType());
    assertThat(hashSet, Matchers.hasEntry("template", Util.readResource("/plugin-settings.template.html")));
  }

  @Test
  void allFieldsShouldBePresentInView() throws Exception {
    String template = Util.readResource("/plugin-settings.template.html");

    for (Map.Entry<String, Field> fieldEntry : GetClusterProfileMetadataExecutor.CLUSTER_PROFILE_FIELDS.entrySet()) {
      assertThat(template, containsString("ng-model=\"" + fieldEntry.getKey() + "\""));
      assertThat(template, containsString("<span class=\"form_error\" ng-class=\"{'is-visible': GOINPUTNAME[" +
          fieldEntry.getKey() + "].$error.server}\" ng-show=\"GOINPUTNAME[" + fieldEntry.getKey() +
          "].$error.server\">{{GOINPUTNAME[" + fieldEntry.getKey() +
          "].$error.server}}</span>"));
    }
  }

}
