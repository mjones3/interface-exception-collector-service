package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for SourceServiceClientRegistry.
 */
@ExtendWith(MockitoExtension.class)
class SourceServiceClientRegistryTest {

    @Mock
    private SourceServiceClient orderClient;

    @Mock
    private SourceServiceClient collectionClient;

    @Mock
    private SourceServiceClient distributionClient;

    private SourceServiceClientRegistry registry;

    @BeforeEach
    void setUp() {
        // Setup mock clients with lenient stubbing to avoid unnecessary stubbing errors
        lenient().when(orderClient.supports("ORDER")).thenReturn(true);
        lenient().when(orderClient.supports("COLLECTION")).thenReturn(false);
        lenient().when(orderClient.supports("DISTRIBUTION")).thenReturn(false);
        lenient().when(orderClient.supports("UNKNOWN")).thenReturn(false);
        lenient().when(orderClient.getServiceName()).thenReturn("order-service");

        lenient().when(collectionClient.supports("ORDER")).thenReturn(false);
        lenient().when(collectionClient.supports("COLLECTION")).thenReturn(true);
        lenient().when(collectionClient.supports("DISTRIBUTION")).thenReturn(false);
        lenient().when(collectionClient.supports("UNKNOWN")).thenReturn(false);
        lenient().when(collectionClient.getServiceName()).thenReturn("collection-service");

        lenient().when(distributionClient.supports("ORDER")).thenReturn(false);
        lenient().when(distributionClient.supports("COLLECTION")).thenReturn(false);
        lenient().when(distributionClient.supports("DISTRIBUTION")).thenReturn(true);
        lenient().when(distributionClient.supports("UNKNOWN")).thenReturn(false);
        lenient().when(distributionClient.getServiceName()).thenReturn("distribution-service");

        List<SourceServiceClient> clients = List.of(orderClient, collectionClient, distributionClient);
        registry = new SourceServiceClientRegistry(clients);
    }

    @Test
    void testGetClient_ByInterfaceType() {
        // When & Then
        assertEquals(orderClient, registry.getClient(InterfaceType.ORDER));
        assertEquals(collectionClient, registry.getClient(InterfaceType.COLLECTION));
        assertEquals(distributionClient, registry.getClient(InterfaceType.DISTRIBUTION));
    }

    @Test
    void testGetClient_ByString() {
        // When & Then
        assertEquals(orderClient, registry.getClient("ORDER"));
        assertEquals(collectionClient, registry.getClient("COLLECTION"));
        assertEquals(distributionClient, registry.getClient("DISTRIBUTION"));
    }

    @Test
    void testGetClient_UnsupportedType() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> registry.getClient("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> registry.getClient("INVALID"));
    }

    @Test
    void testHasClient_ByInterfaceType() {
        // When & Then
        assertTrue(registry.hasClient(InterfaceType.ORDER));
        assertTrue(registry.hasClient(InterfaceType.COLLECTION));
        assertTrue(registry.hasClient(InterfaceType.DISTRIBUTION));
    }

    @Test
    void testHasClient_ByString() {
        // When & Then
        assertTrue(registry.hasClient("ORDER"));
        assertTrue(registry.hasClient("COLLECTION"));
        assertTrue(registry.hasClient("DISTRIBUTION"));
        assertFalse(registry.hasClient("UNKNOWN"));
    }

    @Test
    void testGetAllClients() {
        // When
        List<SourceServiceClient> allClients = registry.getAllClients();

        // Then
        assertEquals(3, allClients.size());
        assertTrue(allClients.contains(orderClient));
        assertTrue(allClients.contains(collectionClient));
        assertTrue(allClients.contains(distributionClient));
    }

    @Test
    void testGetAllClients_IsImmutable() {
        // When
        List<SourceServiceClient> allClients = registry.getAllClients();

        // Then - should not be able to modify the returned list
        assertThrows(UnsupportedOperationException.class, () -> allClients.clear());
    }
}