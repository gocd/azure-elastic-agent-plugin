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

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.utils.Util;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GetPluginSettingsIconExecutorTest {

  @Test
  void rendersIconInBase64() throws Exception {
    GoPluginApiResponse response = new GetPluginSettingsIconExecutor().execute();
    HashMap<String, String> hashMap = new Gson().fromJson(response.responseBody(), new TypeToken<HashMap<String, String>>() {
    }.getType());
    assertThat(hashMap.size(), is(2));
    assertThat(hashMap.get("content_type"), is("image/svg+xml"));
    System.out.println("hashMap = " + hashMap.get("data"));
    assertThat(Util.readResourceBytes("/plugin-icon.svg"), is(BaseEncoding.base64().decode(hashMap.get("data"))));
  }
}
