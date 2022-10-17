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

package com.thoughtworks.gocd.elasticagent.azure.requests;

import com.thoughtworks.gocd.elasticagent.azure.Clock;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class RequestFingerprintCacheTest {

  @Mock
  TestClass mockExecutor;

  @BeforeEach
  void setup() {
    openMocks(this);
  }

  @Test
  void shouldAddRequestFingerprintForANewRequestAndExecuteTheCommandPassed() throws Exception {
    RequestFingerprintCache cache = new RequestFingerprintCache();

    cache.getOrExecute("new-request", Period.minutes(2), mockExecutor::execute);

    verify(mockExecutor, times(1)).execute();
    assertNotNull(cache.get("new-request"));
  }

  @Test
  void shouldNotExecuteTheCommandForAnAlreadyregisteredRequestFingerprintBeforeTimeout() throws Exception {
    Clock mockClock = mock(Clock.class);
    DateTime firstRequestInstance = DateTime.now();
    when(mockClock.now()).thenReturn(firstRequestInstance, firstRequestInstance.plusMinutes(1));
    RequestFingerprintCache cache = new RequestFingerprintCache(mockClock);
    cache.put("existing-request");

    cache.getOrExecute("existing-request", Period.minutes(2), () -> mockExecutor.execute());

    verify(mockExecutor, never()).execute();
    verify(mockClock, times(2)).now();
    assertEquals(firstRequestInstance, cache.get("existing-request"));
  }

  @Test
  void shouldClearRequestCache() {
    RequestFingerprintCache cache = new RequestFingerprintCache();

    cache.put("req-fingerprint");

    cache.clear("req-fingerprint");
    assertNull(cache.get("req-fingerprint"));
  }

  @Test
  void shouldClearCacheOnExceptionInCommandExecution() throws Exception {
    assertThrows(Exception.class, () -> {
      RequestFingerprintCache cache = new RequestFingerprintCache();
      TestClass mockExecutor = mock(TestClass.class);
      when(mockExecutor.execute()).thenThrow(new Exception());

      cache.getOrExecute("new-request", Period.minutes(2), mockExecutor::execute);
      assertNull(cache.get("new-request"));
    });
  }

  @Test
  void shouldRetryCommandExecutionIfThereIsARetryRequestAfterTimeout() throws Exception {
    Clock mockClock = mock(Clock.class);
    RequestFingerprintCache cache = new RequestFingerprintCache(mockClock);
    DateTime now = DateTime.now();
    Period timeoutPeriod = Period.minutes(2);
    DateTime requestTimeAfterTimeout = now.plusMinutes(1 + timeoutPeriod.getMinutes());
    when(mockClock.now()).thenReturn(now, requestTimeAfterTimeout, requestTimeAfterTimeout);

    cache.put("finger-print");
    cache.getOrExecute("finger-print", timeoutPeriod, mockExecutor::execute);

    verify(mockClock, times(3)).now();
    assertEquals(requestTimeAfterTimeout, cache.get("finger-print"));
    verify(mockExecutor, times(1)).execute();
  }

  class TestClass {
    boolean execute() throws Exception {
      System.out.println("Test class executed");
      return true;
    }
  }

}
