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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryMetadataTest {

  @Test
  void shouldValidateMemoryBytes() {
    assertTrue(new MemoryMetadata("Disk", false).validate("100mb").isEmpty());

    Map<String, String> validate = new MemoryMetadata("Disk", false).validate("xxx");
    assertThat(validate.size(), is(1));
    assertThat(validate, hasEntry("Disk", "Invalid size: xxx"));
  }

  @Test
  void shouldValidateMemoryBytesWhenRequireField() {
    Map<String, String> validate = new MemoryMetadata("Disk", true).validate(null);
    assertThat(validate.size(), is(1));
    assertThat(validate.get("Disk"), containsString("Disk must not be blank."));
  }
}
