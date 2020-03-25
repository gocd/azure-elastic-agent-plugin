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

public class Errors {

  public static String AZURE_AUTHENTICATION_ERROR = "Invalid Azure auth credentials";
  public static String AZURE_INVALID_SUBNET_MESSAGE_FORMAT = "%s is not a valid subnet present in the selected Virtual Network.";
  public static String AZURE_INVALID_NETWORK_ID = "Please provide a valid Network Id.";
  public static String AZURE_INVALID_NSG_ID = "Please provide a valid Network Security Group Id.";
  public static String AZURE_INVALID_RESOURCE_GROUP = "Please provide an existing resource group.";
  public static String AZURE_INVALID_REGION = "Please provide a valid Azure Region.";

}
