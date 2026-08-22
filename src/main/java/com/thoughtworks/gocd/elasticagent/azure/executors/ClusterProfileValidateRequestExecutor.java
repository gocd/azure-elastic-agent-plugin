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

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import static com.thoughtworks.gocd.elasticagent.azure.executors.GetClusterProfileMetadataExecutor.CLUSTER_PROFILE_FIELDS;
import com.thoughtworks.gocd.elasticagent.azure.models.Field;
import com.thoughtworks.gocd.elasticagent.azure.requests.ClusterProfileValidateRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterProfileValidateRequestExecutor implements RequestExecutor {

  private final ClusterProfileValidateRequest request;
  private static final Gson GSON = new Gson();

  public ClusterProfileValidateRequestExecutor(ClusterProfileValidateRequest request) {
    this.request = request;
  }

  @Override
  public GoPluginApiResponse execute() throws Exception {
    ArrayList<Map<String, String>> result = new ArrayList<>();

    List<String> knownFields = new ArrayList<>();

    for (Field field : CLUSTER_PROFILE_FIELDS.values()) {
      knownFields.add(field.key());
      Map<String, String> validationError = field.validate(request.getProperties().get(field.key()));

      if (!validationError.isEmpty()) {
        result.add(validationError);
      }
    }

    Set<String> set = new HashSet<>(request.getProperties().keySet());
    set.removeAll(knownFields);

    if (!set.isEmpty()) {
      for (String key : set) {
        LinkedHashMap<String, String> validationError = new LinkedHashMap<>();
        validationError.put("key", key);
        validationError.put("message", "Is an unknown property");
        result.add(validationError);
      }
    }

    return DefaultGoPluginApiResponse.success(GSON.toJson(result));
  }
}
