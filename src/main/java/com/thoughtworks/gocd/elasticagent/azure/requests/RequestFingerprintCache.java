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

package com.thoughtworks.gocd.elasticagent.azure.requests;

import com.thoughtworks.gocd.elasticagent.azure.Clock;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;

public class RequestFingerprintCache {

  private Map<String, DateTime> requestsFired;
  private Clock clock = Clock.DEFAULT;

  public RequestFingerprintCache() {
    this.requestsFired = new ConcurrentHashMap<>();
  }

  public RequestFingerprintCache(Clock clock) {
    this();
    this.clock = clock;
  }

  void put(String key) {
    LOG.info("Adding request fingerprint {} to the cache", key);
    requestsFired.put(key, clock.now());
  }

  DateTime get(String key) {
    return requestsFired.get(key);
  }

  public void clear(String key) {
    LOG.info("Clearing request fingerprint {} from the cache", key);
    requestsFired.remove(key);
  }

  public void getOrExecute(String requestFingerprint, Period timeout, SupplierThrowingException command) throws Exception {
    LOG.debug("Checking for request fingerprint {} in the cache", requestFingerprint);
    if (shouldExecuteCommand(requestFingerprint, timeout)) {
      LOG.debug("Processing new request with fingerprint {} ", requestFingerprint);
      try {
        put(requestFingerprint);
        command.get();
      } catch (Exception e) {
        clear(requestFingerprint);
        throw e;
      }
    } else {
      LOG.debug("Request with fingerprint {} already in progress. Ignoring the request.", requestFingerprint);
    }

  }

  private boolean shouldExecuteCommand(String requestFingerprint, Period timeout){
    DateTime requestTimestamp = get(requestFingerprint);
    return requestTimestamp == null || clock.now().isAfter(requestTimestamp.plus(timeout));
  }

  public interface SupplierThrowingException {
    void get() throws Exception;
  }
}
