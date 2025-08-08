package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Registry for managing source service clients and routing requests
 * to the appropriate client based on interface type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SourceServiceClientRegistry {

    private final List<SourceServiceClient> clients;

    /**
     * Gets the appropriate source service client for the given interface type.
     *
     * @param interfaceType the interface type
     * @return the matching client
     * @throws IllegalArgumentException if no client supports the interface type
     */
    public SourceServiceClient getClient(InterfaceType interfaceType) {
        return getClient(interfaceType.name());
    }

    /**
     * Gets the appropriate source service client for the given interface type.
     *
     * @param interfaceType the interface type as string
     * @return the matching client
     * @throws IllegalArgumentException if no client supports the interface type
     */
    public SourceServiceClient getClient(String interfaceType) {
        Optional<SourceServiceClient> client = clients.stream()
                .filter(c -> c.supports(interfaceType))
                .findFirst();

        if (client.isEmpty()) {
            log.error("No source service client found for interface type: {}", interfaceType);
            throw new IllegalArgumentException("Unsupported interface type: " + interfaceType);
        }

        log.debug("Found client {} for interface type: {}", client.get().getServiceName(), interfaceType);
        return client.get();
    }

    /**
     * Gets all registered clients.
     *
     * @return list of all clients
     */
    public List<SourceServiceClient> getAllClients() {
        return List.copyOf(clients);
    }

    /**
     * Checks if a client exists for the given interface type.
     *
     * @param interfaceType the interface type
     * @return true if a client exists, false otherwise
     */
    public boolean hasClient(InterfaceType interfaceType) {
        return hasClient(interfaceType.name());
    }

    /**
     * Checks if a client exists for the given interface type.
     *
     * @param interfaceType the interface type as string
     * @return true if a client exists, false otherwise
     */
    public boolean hasClient(String interfaceType) {
        return clients.stream().anyMatch(c -> c.supports(interfaceType));
    }
}