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

package com.thoughtworks.gocd.elasticagent.azure.service;

import com.thoughtworks.gocd.elasticagent.azure.PluginRequest;
import com.thoughtworks.gocd.elasticagent.azure.exceptions.ServerRequestFailedException;
import com.thoughtworks.gocd.elasticagent.azure.models.PluginHealthMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerHealthMessagingServiceTest {

  @Mock
  private PluginRequest mockPluginRequest;

  @Captor
  private ArgumentCaptor<List<PluginHealthMessage>> messagesCaptor;

  private ServerHealthMessagingService serverHealthMessagingService;

  @BeforeEach
  void setUp() {
    serverHealthMessagingService = new ServerHealthMessagingService(mockPluginRequest);
  }

  @Test
  void testShouldSendHealthMessage() throws ServerRequestFailedException {
    PluginHealthMessage warningMessage = PluginHealthMessage.warning("a warning message");

    serverHealthMessagingService.sendHealthMessage("message-key", warningMessage);

    verify(mockPluginRequest).sendHealthMessages(asList(warningMessage));
  }

  @Test
  void testShouldSendPreviousMessagesWhenSendingHealthMessage() throws ServerRequestFailedException {
    PluginHealthMessage warningMessage = PluginHealthMessage.warning("a warning message");
    PluginHealthMessage errorMessage = PluginHealthMessage.error("a error message");

    serverHealthMessagingService.sendHealthMessage("message-key", warningMessage);
    serverHealthMessagingService.sendHealthMessage("message-key2", errorMessage);

    verify(mockPluginRequest, times(2)).sendHealthMessages(messagesCaptor.capture());
    List<List<PluginHealthMessage>> allValues = messagesCaptor.getAllValues();
    assertEquals(2, allValues.size());

    List<PluginHealthMessage> firstCallMessages = allValues.get(0);
    assertEquals(asList(warningMessage), firstCallMessages);

    List<PluginHealthMessage> secondCallMessages = allValues.get(1);
    assertEquals(2, secondCallMessages.size());
    assertTrue(secondCallMessages.contains(warningMessage));
    assertTrue(secondCallMessages.contains(errorMessage));
  }

  @Test
  void testClearHealthMessageShouldMakeCallWithOtherMessages() throws ServerRequestFailedException {
    PluginHealthMessage message1 = PluginHealthMessage.warning("a warning message");
    PluginHealthMessage clearedMessage = PluginHealthMessage.error("a error message");

    serverHealthMessagingService.sendHealthMessage("message-key", message1);
    serverHealthMessagingService.sendHealthMessage("message-key2", clearedMessage);
    serverHealthMessagingService.clearHealthMessage("message-key2");

    verify(mockPluginRequest, times(3)).sendHealthMessages(messagesCaptor.capture());
    List<List<PluginHealthMessage>> allValues = messagesCaptor.getAllValues();
    assertEquals(3, allValues.size());

    List<PluginHealthMessage> lastCallMessages = allValues.get(2);
    assertEquals(1, lastCallMessages.size());
    assertTrue(lastCallMessages.contains(message1));
  }

  @Test
  void testShouldClearExpiredMessages() throws ServerRequestFailedException {
    PluginHealthMessage message1 = mock(PluginHealthMessage.class);
    PluginHealthMessage message2 = mock(PluginHealthMessage.class);
    PluginHealthMessage message3 = mock(PluginHealthMessage.class);
    when(message1.isExpired()).thenReturn(false);
    when(message2.isExpired()).thenReturn(true);
    when(message3.isExpired()).thenReturn(false);

    serverHealthMessagingService.sendHealthMessage("message-1", message1);
    serverHealthMessagingService.sendHealthMessage("message-2", message2);
    serverHealthMessagingService.sendHealthMessage("message-3", message3);
    serverHealthMessagingService.clearExpiredHealthMessages();

    verify(mockPluginRequest, times(4)).sendHealthMessages(messagesCaptor.capture());
    assertEquals(4, messagesCaptor.getAllValues().size());

    List<PluginHealthMessage> clearMessagesSent = messagesCaptor.getAllValues().get(3);
    assertEquals(2, clearMessagesSent.size());
    assertTrue(clearMessagesSent.contains(message1));
    assertTrue(clearMessagesSent.contains(message3));
  }

  @Test
  void testShouldNotMakeServerRequestWhenNoExpiredMessages() throws ServerRequestFailedException {
    PluginHealthMessage message1 = mock(PluginHealthMessage.class);
    PluginHealthMessage message2 = mock(PluginHealthMessage.class);
    when(message1.isExpired()).thenReturn(false);
    when(message2.isExpired()).thenReturn(false);

    serverHealthMessagingService.sendHealthMessage("message-1", message1);
    serverHealthMessagingService.sendHealthMessage("message-2", message2);
    serverHealthMessagingService.clearExpiredHealthMessages();

    verify(mockPluginRequest, times(2)).sendHealthMessages(messagesCaptor.capture());
    List<List<PluginHealthMessage>> allValues = messagesCaptor.getAllValues();
    assertEquals(2, allValues.size());
    assertEquals(asList(message1), allValues.get(0));

    assertEquals(2, allValues.get(1).size());
    assertTrue(allValues.get(1).contains(message1));
    assertTrue(allValues.get(1).contains(message2));
  }
}
