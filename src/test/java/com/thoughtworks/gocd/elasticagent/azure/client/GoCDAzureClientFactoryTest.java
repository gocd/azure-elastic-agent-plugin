package com.thoughtworks.gocd.elasticagent.azure.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class GoCDAzureClientFactoryTest {
    @Test
    void shouldGetSubscriptionIDFromNetworkID() throws IOException {
        verifySubscriptionId("/subscriptions/abcdef98-0123-4567-890a-fedcba01/resourceGroups/", "abcdef98-0123-4567-890a-fedcba01");
        verifySubscriptionId("/somethingelse", "/somethingelse");
        verifySubscriptionId("", "");
    }

    private void verifySubscriptionId(String networkID, String expectedSubscriptionID) throws IOException {
        GoCDAzureClientFactory factory = spy(GoCDAzureClientFactory.class);
        when(factory.createClient(anyString(), anyString(), anyString(), anyString(), eq(expectedSubscriptionID))).thenReturn(mock(GoCDAzureClient.class));

        factory.initialize("clientID1", "domainID1", "secret1", "resourceGroup1", networkID);

        verify(factory).createClient(eq("clientID1"), eq("domainID1"), eq("secret1"), eq("resourceGroup1"), eq(expectedSubscriptionID));
    }
}