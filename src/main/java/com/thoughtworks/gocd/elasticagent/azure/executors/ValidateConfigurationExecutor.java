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

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.models.ValidationResult;
import com.thoughtworks.gocd.elasticagent.azure.validations.Validation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;
import static com.thoughtworks.gocd.elasticagent.azure.Errors.AZURE_AUTHENTICATION_ERROR;
import static com.thoughtworks.gocd.elasticagent.azure.executors.GetClusterProfileMetadataExecutor.*;

public class ValidateConfigurationExecutor implements RequestExecutor {

    private final Map<String, String> settings;
    private final List<Validation> validations;
    private GoCDAzureClientFactory goCDAzureClientFactory;

    public ValidateConfigurationExecutor(Map<String, String> settings, GoCDAzureClientFactory goCDAzureClientFactory, List<Validation> validations) {
        this.settings = settings;
        this.goCDAzureClientFactory = goCDAzureClientFactory;
        this.validations = validations;
    }

    public GoPluginApiResponse execute() {
        ValidationResult validationResult = new ValidationResult();
        try {
            GoCDAzureClient client = getGoCDAzureClient(settings);
            validations.forEach(validation -> validationResult.addErrors(validation.run(settings, client)));
        } catch (IOException | RuntimeException ex) {
            LOG.debug("Azure AuthenticationError ", ex);
            validationResult.addError(CLIENT_ID.key(), AZURE_AUTHENTICATION_ERROR);
            validationResult.addError(SECRET.key(), AZURE_AUTHENTICATION_ERROR);
            validationResult.addError(DOMAIN.key(), AZURE_AUTHENTICATION_ERROR);
        }
        return DefaultGoPluginApiResponse.success(validationResult.toJson());
    }

    private GoCDAzureClient getGoCDAzureClient(Map<String, String> connectionParams) throws IOException {
        return goCDAzureClientFactory.initialize(
                connectionParams.get(CLIENT_ID.key()),
                connectionParams.get(DOMAIN.key()),
                connectionParams.get(SECRET.key()),
                connectionParams.get(RESOURCE_GROUP.key()),
                connectionParams.get(NETWORK_ID.key()));
    }
}
