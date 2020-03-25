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

package com.thoughtworks.gocd.elasticagent.azure.executors;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.PluginSettings;
import com.thoughtworks.gocd.elasticagent.azure.RequestExecutor;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClient;
import com.thoughtworks.gocd.elasticagent.azure.client.GoCDAzureClientFactory;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.PluginSettingsNotConfiguredException;
import com.thoughtworks.gocd.elasticagent.azure.models.ValidationResult;
import com.thoughtworks.gocd.elasticagent.azure.requests.ProfileValidateRequest;
import com.thoughtworks.gocd.elasticagent.azure.validations.Validation;

import java.util.List;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;

public class ProfileValidateRequestExecutor implements RequestExecutor {
  public static final String PLUGIN_SETTINGS_NOT_CONFIGURED_MESSAGE = "Azure plugin settings not configured.";
  public static final String UNEXPECTED_ERROR_MESSAGE = "Validation failed due to unexpected error.";
  private final ProfileValidateRequest request;
  private final PluginRequest pluginRequest;
  private final GoCDAzureClientFactory goCDAzureClientFactory;
  private List<Validation> validations;

  public ProfileValidateRequestExecutor(ProfileValidateRequest request, PluginRequest pluginRequest, GoCDAzureClientFactory goCDAzureClientFactory, List<Validation> validations) {
    this.request = request;
    this.pluginRequest = pluginRequest;
    this.goCDAzureClientFactory = goCDAzureClientFactory;
    this.validations = validations;
  }

  @Override
  public GoPluginApiResponse execute() {
    ValidationResult validationResult = new ValidationResult();
    try {
      PluginSettings pluginSettings = pluginRequest.getPluginSettings();
      GoCDAzureClient goCDAzureClient = goCDAzureClientFactory.initialize(pluginSettings);
      validations.forEach(validation -> validationResult.addErrors(validation.run(request.getProperties(), pluginSettings, goCDAzureClient)));
      return DefaultGoPluginApiResponse.success(validationResult.toJson());
    } catch (PluginSettingsNotConfiguredException e) {
      return DefaultGoPluginApiResponse.success(errorResult(validationResult, PLUGIN_SETTINGS_NOT_CONFIGURED_MESSAGE).toJson());
    } catch (Exception e) {
      LOG.error("Failed to validated Profile due to error: {}", e.getMessage());
      return DefaultGoPluginApiResponse.success(errorResult(validationResult, UNEXPECTED_ERROR_MESSAGE).toJson());
    }
  }

  private ValidationResult errorResult(ValidationResult validationResult, String s) {
    GetProfileMetadataExecutor.FIELDS.forEach(metadata -> validationResult.addError(metadata.getKey(), s));
    return validationResult;
  }
}
