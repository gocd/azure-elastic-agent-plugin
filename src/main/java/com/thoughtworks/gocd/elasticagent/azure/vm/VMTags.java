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

package com.thoughtworks.gocd.elasticagent.azure.vm;

public class VMTags {

  public static final String ENVIRONMENT_TAG_KEY = "environment";
  public static final String JOB_IDENTIFIER_TAG_KEY = "job-identifier";
  public static final String ELASTIC_PROFILE_TAG_KEY = "elastic-profile";
  public static final String GOCD_SERVER_ID_TAG_KEY = "gocd-server-id";
  public static final String LAST_JOB_RUN_TAG_KEY = "last-job-run";
  public static final String IDLE_TIMEOUT = "idle-time-in-mins";
}
