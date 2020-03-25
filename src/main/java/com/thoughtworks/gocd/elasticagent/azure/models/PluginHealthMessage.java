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

package com.thoughtworks.gocd.elasticagent.azure.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.elasticagent.azure.Clock;
import lombok.EqualsAndHashCode;
import org.joda.time.DateTime;
import org.joda.time.Period;

@EqualsAndHashCode
public class PluginHealthMessage {

  public static final Period EXPIRY_PERIOD = Period.hours(12);

  @Expose(serialize = false)
  private final DateTime createdAt;

  @Expose(serialize = false)
  @EqualsAndHashCode.Exclude private final Clock clock;

  @Expose
  @SerializedName("message")
  private String message;

  @Expose
  @SerializedName("type")
  private MessageType type;

  public PluginHealthMessage(String message, MessageType type, Clock clock) {
    this.message = message;
    this.type = type;
    this.clock = clock;
    this.createdAt = clock.now();
  }

  public static PluginHealthMessage error(String message) {
    return new PluginHealthMessage(message, MessageType.error, Clock.DEFAULT);
  }

  public static PluginHealthMessage warning(String message) {
    return new PluginHealthMessage(message, MessageType.warning, Clock.DEFAULT);
  }

  public boolean isExpired() {
    return !clock.now().isBefore(createdAt.plus(EXPIRY_PERIOD));
  }

  enum MessageType {
    error, warning
  }
}
