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

package com.thoughtworks.gocd.elasticagent.azure;

import com.thoughtworks.gocd.elasticagent.azure.AzureInstance.JobState;
import com.thoughtworks.gocd.elasticagent.azure.models.ElasticProfile;
import com.thoughtworks.gocd.elasticagent.azure.models.JobIdentifier;
import com.thoughtworks.gocd.elasticagent.azure.models.Platform;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.vm.VMTags.*;
import static org.junit.jupiter.api.Assertions.*;

class AzureInstanceTest {

  @Test
  void shouldReturnEnvironmentFromTags() {
    Map<String, String> tags = Collections.singletonMap(ENVIRONMENT_TAG_KEY, "Testing");

    AzureInstance instance = new AzureInstance(null, null, null, null,
        null, null, null, null, null, null,
        null, null, tags, Platform.LINUX);

    assertEquals("Testing", instance.getEnvironment());
  }

  @Test
  void shouldCheckIfJobIdentifierMatchesFromTags() {
    JobIdentifier jobIdentifier = new JobIdentifier(1L);
    Map<String, String> tags = Collections.singletonMap(JOB_IDENTIFIER_TAG_KEY, jobIdentifier.hash());

    AzureInstance instance = new AzureInstance(null, null, null, null,
        null, null, null, null, null, null,
        null, null, tags, Platform.WINDOWS);

    assertTrue(instance.jobIdentifierMatches(jobIdentifier));
    assertFalse(instance.jobIdentifierMatches(new JobIdentifier(5L)));
  }

  @Test
  void shouldNotBeAssignableWhenElasticProfileDoesNotMatchFromTags() {
    Map<String, String> tags = Collections.singletonMap(ELASTIC_PROFILE_TAG_KEY, "random-hash");

    AzureInstance instance = new AzureInstance(null, null, null, null,
        null, null, null, null, null, null,
        null, null, tags, Platform.LINUX);


    assertFalse(instance.canBeAssigned(new ElasticProfile()));
  }

  @Test
  void shouldReturnDefaultsEnvironmentIfTagsAreNotSet() {
    AzureInstance instance = new AzureInstance(null, null, null, null,
        null, null, null, null, null, null,
        null, null, Collections.emptyMap(), Platform.LINUX);

    assertEquals("", instance.getEnvironment());
  }

  @Test
  void shouldBeIdleAfterTimeoutWhenCreatedInThePastAndIdleTimeoutNotSet() {
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now().minusMinutes(1),
        null, null, null, null, null, null,
        null, null, Collections.emptyMap(), Platform.WINDOWS);

    assertTrue(instance.isIdleAfterIdleTimeout());
  }

  @Test
  void shouldBeAbleToTerminateInstancePastIdleTimeoutWhichHasBeenAssignedBefore() {
    Map<String, String> tags = new HashMap<>();
    DateTime lastJobRunTime = Clock.DEFAULT.now().minusMinutes(2);
    tags.put(LAST_JOB_RUN_TAG_KEY, getMillis(lastJobRunTime));
    tags.put(IDLE_TIMEOUT, "0");

    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now().minusMinutes(1),
        null, null, null, null, null, null,
        null, null, tags, Platform.WINDOWS);

    assertTrue(instance.canBeTerminated());
  }

  @Test
  void shouldNotTerminateInstancePastIdleTimeoutWhichHasNeverBeenAssignedBefore() {
    Map<String, String> tags = new HashMap<>();
    tags.put(IDLE_TIMEOUT, "0");

    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now().minusMinutes(1),
        null, null, null, null, null, null,
        null, null, tags, Platform.WINDOWS);

    assertFalse(instance.canBeTerminated());
  }

  @Test
  void shouldBeIdleAfterTimeoutIfIdleTimeoutIsSetAndNeverAssignedBefore() {
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now(),
        null, null, null, null, null, null,
        null, null,
        Collections.singletonMap(IDLE_TIMEOUT, "10"), Platform.LINUX);

    assertFalse(instance.isIdleAfterIdleTimeout());
  }

  @Test
  void shouldBeIdleAfterTimeoutIfIdleTimeoutIsZero() {
    HashMap<String, String> tags = new HashMap<>();
    tags.put(IDLE_TIMEOUT, "0");
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now().minusMillis(100),
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertTrue(instance.isIdleAfterIdleTimeout());
  }

  @Test
  void shouldBeIdleAfterTimeoutIfLastJobRunTimestampIsSetAndIdleTimeoutIsZero() {
    DateTime now = DateTime.now();
    DateTime provisionTime = now.minusMinutes(5);
    DateTime lastJobRunTime = now.minusMinutes(2);
    HashMap<String, String> tags = new HashMap<>();
    tags.put(LAST_JOB_RUN_TAG_KEY, getMillis(lastJobRunTime));
    tags.put(IDLE_TIMEOUT, "0");
    AzureInstance instance = new AzureInstance(null, null, null, provisionTime,
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertTrue(instance.isIdleAfterIdleTimeout());
  }

  @Test
  void shouldBeIdleAfterTimeoutIfLastJobRunPlusTimeoutIsInThePast() {
    DateTime now = DateTime.now();
    DateTime provisionTime = now.minusMinutes(15);
    DateTime lastJobRunTime = now.minusMinutes(10);
    HashMap<String, String> tags = new HashMap<>();
    tags.put(LAST_JOB_RUN_TAG_KEY, getMillis(lastJobRunTime));
    tags.put(IDLE_TIMEOUT, "5");

    AzureInstance instance = new AzureInstance(null, null, null, provisionTime,
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertTrue(instance.isIdleAfterIdleTimeout());
  }

  @Test
  void shouldNotBeIdleAfterTimeoutIfLastJobRunTimestampPlusIdleTimeoutIsInTheFuture() {
    DateTime now = DateTime.now();
    DateTime provisionTime = now.minusMinutes(15);
    DateTime lastJobRunTime = now.minusMinutes(5);
    HashMap<String, String> tags = new HashMap<>();
    tags.put(LAST_JOB_RUN_TAG_KEY, getMillis(lastJobRunTime));
    tags.put(IDLE_TIMEOUT, "6");

    AzureInstance instance = new AzureInstance(null, null, null, provisionTime,
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertFalse(instance.isIdleAfterIdleTimeout());
  }

  @Test
  void shouldBeAssignedIfJobIdentifierTagSet() {
    DateTime now = DateTime.now();
    DateTime provisionTime = now.minusMinutes(5);
    AzureInstance instance = new AzureInstance(null, null, null, provisionTime,
        null, null, null, null, null, null,
        null, null,
        Collections.singletonMap(JOB_IDENTIFIER_TAG_KEY, "jobid"), Platform.LINUX);

    assertTrue(instance.isAssigned());
  }

  @Test
  void shouldBeAssignedIfJobIdentifierTagNotSet() {
    DateTime now = DateTime.now();
    DateTime provisionTime = now.minusMinutes(5);
    AzureInstance instance = new AzureInstance(null, null, null, provisionTime,
        null, null, null, null, null, null,
        null, null,
        Collections.emptyMap(), Platform.LINUX);

    assertFalse(instance.isAssigned());
  }

  @Test
  void shouldBeAssignedIfJobIdentifierTagEmpty() {
    DateTime now = DateTime.now();
    DateTime provisionTime = now.minusMinutes(5);
    AzureInstance instance = new AzureInstance(null, null, null, provisionTime,
        null, null, null, null, null, null,
        null, null,
        Collections.singletonMap(JOB_IDENTIFIER_TAG_KEY, " "), Platform.LINUX);

    assertFalse(instance.isAssigned());
  }

  @Test
  void shouldBeInBuildingStateIfAssigned() {
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now(),
        null, null, null, null, null, null,
        null, null,
        Collections.singletonMap(JOB_IDENTIFIER_TAG_KEY, "jobid"), Platform.LINUX);

    assertEquals(JobState.Assigned, instance.getJobState());
  }

  @Test
  void shouldBeInIdleStateIfUnassigned() {
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now(),
        null, null, null, null, null, null,
        null, null,
        Collections.emptyMap(), Platform.LINUX);

    assertEquals(JobState.Unassigned, instance.getJobState());
  }

  @Test
  void shouldNotBeAssignableWhenElasticProfileHashDoesNotMatch() {
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now(),
        null, null, null, null, null, null,
        null, null,
        Collections.singletonMap(ELASTIC_PROFILE_TAG_KEY, "random"), Platform.LINUX);

    assertFalse(instance.canBeAssigned(new ElasticProfile()));
  }

  @Test
  void shouldNotBeAssignableWhenElasticProfileMatchesButInstanceIsAlreadyAssigned() {
    ElasticProfile elasticProfile = new ElasticProfile();
    HashMap<String, String> tags = new HashMap<>();
    tags.put(ELASTIC_PROFILE_TAG_KEY, elasticProfile.hash());
    tags.put(JOB_IDENTIFIER_TAG_KEY, "job-id");
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now(),
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertFalse(instance.canBeAssigned(elasticProfile));
  }

  @Test
  void shouldNotBeAssignableWhenIdleAfterIdleTimeout() {
    ElasticProfile elasticProfile = new ElasticProfile();
    HashMap<String, String> tags = new HashMap<>();
    tags.put(ELASTIC_PROFILE_TAG_KEY, elasticProfile.hash());
    tags.put(IDLE_TIMEOUT, "10");
    tags.put(LAST_JOB_RUN_TAG_KEY, getMillis(DateTime.now().minusMinutes(15)));
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now().minusMinutes(20),
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertFalse(instance.canBeAssigned(elasticProfile));
  }

  @Test
  void shouldAssignableWhenElasticProfileMatchesAndVMIsIdleWithinTimeout() {
    ElasticProfile elasticProfile = new ElasticProfile();
    HashMap<String, String> tags = new HashMap<>();
    tags.put(ELASTIC_PROFILE_TAG_KEY, elasticProfile.hash());
    tags.put(IDLE_TIMEOUT, "10");
    tags.put(LAST_JOB_RUN_TAG_KEY, getMillis(DateTime.now().minusMinutes(5)));
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now().minusMinutes(15),
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertTrue(instance.canBeAssigned(elasticProfile));
  }

  @Test
  void shouldBeAssignableWhenInstanceNeverAssignedBeforeAndIdleTimeoutIsZero() {
    ElasticProfile elasticProfile = new ElasticProfile();
    HashMap<String, String> tags = new HashMap<>();
    tags.put(ELASTIC_PROFILE_TAG_KEY, elasticProfile.hash());
    tags.put(IDLE_TIMEOUT, "0");
    AzureInstance instance = new AzureInstance(null, null, null, DateTime.now(),
        null, null, null, null, null, null,
        null, null,
        tags, Platform.LINUX);

    assertTrue(instance.canBeAssigned(elasticProfile));
  }

  private String getMillis(DateTime lastJobRunTime) {
    return String.valueOf(lastJobRunTime.toInstant().getMillis());
  }
}
