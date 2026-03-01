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

package com.thoughtworks.gocd.elasticagent.azure.utils;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.thoughtworks.gocd.elasticagent.azure.executors.GetClusterProfileViewRequestExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class Util {

  public static final String PLUGIN_VERSION_KEY = "version";
  public static final String PLUGIN_ID_KEY = "id";
  private static Random random = new Random();

  public static String readResource(String resourceFile) {
    try (InputStreamReader reader = new InputStreamReader(GetClusterProfileViewRequestExecutor.class.getResourceAsStream(resourceFile), StandardCharsets.UTF_8)) {
      return CharStreams.toString(reader);
    } catch (IOException e) {
      throw new RuntimeException("Could not find resource " + resourceFile, e);
    }
  }

  public static byte[] readResourceBytes(String resourceFile) {
    try (InputStream in = GetClusterProfileViewRequestExecutor.class.getResourceAsStream(resourceFile)) {
      return ByteStreams.toByteArray(in);
    } catch (IOException e) {
      throw new RuntimeException("Could not find resource " + resourceFile, e);
    }
  }

  public static String pluginId() {
    return pluginProperty(PLUGIN_ID_KEY);
  }

  public static String pluginVersion() {
    return pluginProperty(PLUGIN_VERSION_KEY);
  }

  public static String uniqueString(String prefix) {
    return String.format("%s-%s", prefix, UUID.randomUUID());
  }

  public static String[] splitByComma(String string) {
    return string.trim().split("\\s*,\\s*");
  }

  private static String pluginProperty(String key) {
    String s = readResource("/plugin.properties");
    try {
      Properties properties = new Properties();
      properties.load(new StringReader(s));
      return (String) properties.get(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static int random(int upperBound) {
    return random.nextInt(upperBound);
  }
}
