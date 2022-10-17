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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.PluginSettingsNotConfiguredException;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ServerRequestFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static com.thoughtworks.gocd.elasticagent.azure.models.PluginHealthMessage.error;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class PluginRequestTest {

  @Mock
  private GoApplicationAccessor accessor;

  @BeforeEach
  void setUp() {
    openMocks(this);
  }

  @Test
  void testGetPluginSettingsShouldThrowErrorOnInvalidResponseCode() throws Exception {
    PluginRequest request = new PluginRequest(accessor);
    GoApiResponse response = mock(GoApiResponse.class);
    when(response.responseCode()).thenReturn(500);
    when(accessor.submit(Mockito.any(DefaultGoApiRequest.class))).thenReturn(response);

    assertThrows(ServerRequestFailedException.class, () -> request.getPluginSettings());
  }

  @Test
  void testGetPluginSettingsShouldThrowErrorOnNullResponse() throws Exception {
    PluginRequest request = new PluginRequest(accessor);
    GoApiResponse response = mock(GoApiResponse.class);
    when(response.responseCode()).thenReturn(200);
    when(response.responseBody()).thenReturn(null);
    when(accessor.submit(Mockito.any(DefaultGoApiRequest.class))).thenReturn(response);

    assertThrows(PluginSettingsNotConfiguredException.class, () -> request.getPluginSettings());
  }

  @Test
  void shouldMakeServerInfoV2ApiRequest() throws ServerRequestFailedException {
    PluginRequest request = new PluginRequest(accessor);
    GoApiResponse response = mock(GoApiResponse.class);
    when(response.responseCode()).thenReturn(200);
    when(response.responseBody()).thenReturn(null);
    when(accessor.submit(Mockito.any(DefaultGoApiRequest.class))).thenReturn(response);

    request.getServerInfo();
    ArgumentCaptor<GoApiRequest> requestCaptor = ArgumentCaptor.forClass(GoApiRequest.class);
    verify(accessor, times(1)).submit(requestCaptor.capture());
    GoApiRequest actualRequest = requestCaptor.getValue();

    assertEquals(Constants.SERVER_INFO_PROCESSOR_V2_API_VERSION, actualRequest.apiVersion());
    assertEquals(Constants.REQUEST_SERVER_INFO, actualRequest.api());
  }

  @Test
  void shouldFallbackToServerInfoV1ApiRequestOnV2Error() throws ServerRequestFailedException {
    PluginRequest request = new PluginRequest(accessor);
    GoApiResponse notFoundResponse = mock(GoApiResponse.class);
    when(notFoundResponse.responseCode()).thenReturn(400);
    when(notFoundResponse.responseBody()).thenReturn(null);

    GoApiResponse successResponse = mock(GoApiResponse.class);
    when(successResponse.responseCode()).thenReturn(200);
    when(successResponse.responseBody()).thenReturn(null);
    when(accessor.submit(Mockito.any(DefaultGoApiRequest.class))).thenReturn(notFoundResponse, successResponse);

    request.getServerInfo();

    ArgumentCaptor<GoApiRequest> requestCaptor = ArgumentCaptor.forClass(GoApiRequest.class);
    verify(accessor, times(2)).submit(requestCaptor.capture());
    List<GoApiRequest> actualRequests = requestCaptor.getAllValues();

    assertEquals(Constants.SERVER_INFO_PROCESSOR_V2_API_VERSION, actualRequests.get(0).apiVersion());
    assertEquals(Constants.REQUEST_SERVER_INFO, actualRequests.get(0).api());
    assertEquals(Constants.SERVER_INFO_PROCESSOR_V1_API_VERSION, actualRequests.get(1).apiVersion());
    assertEquals(Constants.REQUEST_SERVER_INFO, actualRequests.get(1).api());
  }

  @Test
  void shouldReturnErrorServerInfoApiRequestError() throws ServerRequestFailedException {
    PluginRequest request = new PluginRequest(accessor);

    GoApiResponse badResponse = mock(GoApiResponse.class);
    when(badResponse.responseCode()).thenReturn(400);
    when(badResponse.responseBody()).thenReturn(null);
    when(accessor.submit(Mockito.any(DefaultGoApiRequest.class))).thenReturn(badResponse, badResponse);

    assertThrows(ServerRequestFailedException.class, () -> request.getServerInfo());

    ArgumentCaptor<GoApiRequest> requestCaptor = ArgumentCaptor.forClass(GoApiRequest.class);
    verify(accessor, times(2)).submit(requestCaptor.capture());
    List<GoApiRequest> actualRequests = requestCaptor.getAllValues();

    assertEquals(Constants.SERVER_INFO_PROCESSOR_V2_API_VERSION, actualRequests.get(0).apiVersion());
    assertEquals(Constants.REQUEST_SERVER_INFO, actualRequests.get(0).api());
    assertEquals(Constants.SERVER_INFO_PROCESSOR_V1_API_VERSION, actualRequests.get(1).apiVersion());
    assertEquals(Constants.REQUEST_SERVER_INFO, actualRequests.get(1).api());


  }

  @Test
  void testSendHealthMessagesShouldThrowErrorOnErrorOrInvalidResponse() throws Exception {
    PluginRequest request = new PluginRequest(accessor);
    GoApiResponse response = mock(GoApiResponse.class);
    when(response.responseCode()).thenReturn(500);
    when(accessor.submit(Mockito.any(DefaultGoApiRequest.class))).thenReturn(response);

    assertThrows(ServerRequestFailedException.class, () -> request.sendHealthMessages(asList(error("error message"))));
  }
}
