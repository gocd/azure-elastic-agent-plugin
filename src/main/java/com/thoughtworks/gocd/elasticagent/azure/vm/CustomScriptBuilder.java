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

import com.thoughtworks.gocd.elasticagent.azure.utils.TemplateReader;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import static com.thoughtworks.gocd.elasticagent.azure.AzurePlugin.LOG;

public class CustomScriptBuilder {

  private String script;

  public CustomScriptBuilder(){ }

  public CustomScriptBuilder withScript(String script){
    this.script = script;
    return this;
  }

  public CustomScriptBuilder withScript(String template, Map<String, String> data) {
    TemplateReader reader = new TemplateReader();
    try {
      this.script = reader.read(template, data);
    } catch (IOException | TemplateException e) {
      LOG.error("Error generating the script file from the template {} and data {} provided: ", template, data, e);
      throw new RuntimeException(e);
    }
    return this;
  }

  public CustomScriptBuilder base64Encoded() {
    if(this.script!= null){
      this.script = Base64.getEncoder().encodeToString(this.script.getBytes());
    }
    return this;
  }

  public String build() {
    return script;
  }
}
