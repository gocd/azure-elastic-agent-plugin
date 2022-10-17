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

package com.thoughtworks.gocd.elasticagent.azure.models;

import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.elasticagent.azure.vm.CustomScriptBuilder;
import com.thoughtworks.gocd.elasticagent.azure.vm.LinuxPlatformConfigStrategy;
import com.thoughtworks.gocd.elasticagent.azure.vm.PlatformConfigStrategy;
import com.thoughtworks.gocd.elasticagent.azure.vm.WindowsPlatformConfigStrategy;
import org.apache.commons.lang3.Range;

public enum Platform {
  @SerializedName("LINUX")
  LINUX {
    private static final int MIN_LINUX_DISK_SIZE = 30;

    @Override
    public PlatformConfigStrategy getConfigStrategy() {
      return new LinuxPlatformConfigStrategy(new CustomScriptBuilder());
    }

    @Override
    public Range<Integer> osDiskSizeRange() {
      return Range.between(MIN_LINUX_DISK_SIZE, MAX_OS_DISK_SIZE);
    }
  },

  @SerializedName("WINDOWS")
  WINDOWS {
    private static final int MIN_WINDOWS_DISK_SIZE = 127;

    @Override
    public PlatformConfigStrategy getConfigStrategy() {
      return new WindowsPlatformConfigStrategy(new CustomScriptBuilder());
    }

    @Override
    public Range<Integer> osDiskSizeRange() {
      return Range.between(MIN_WINDOWS_DISK_SIZE, MAX_OS_DISK_SIZE);
    }
  };

  public static final int MAX_OS_DISK_SIZE = 1023;

  public abstract PlatformConfigStrategy getConfigStrategy();

  public abstract Range<Integer> osDiskSizeRange();
}
