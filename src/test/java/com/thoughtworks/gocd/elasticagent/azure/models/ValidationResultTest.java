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

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

class ValidationResultTest {

  @Test
  void testToJsonGeneratesErrorsWithKeyAndMessage() throws JSONException {
    ValidationResult validationResult = new ValidationResult();
    validationResult.addError("field1", "error1");
    validationResult.addError("field2", "error2");
    JSONAssert.assertEquals("[{\"key\":\"field1\", \"message\":\"error1\"},{\"key\":\"field2\", \"message\":\"error2\"}]", validationResult.toJson(), JSONCompareMode.STRICT);
  }
}
