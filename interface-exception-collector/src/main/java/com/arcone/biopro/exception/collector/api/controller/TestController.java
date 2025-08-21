package com.arcone.biopro.exception.collector.api.controller;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Test controller for debugging database connectivity and repository
 * operations.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Test basic repository connectivity.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getExceptionCount() {
        try {
            long count = exceptionRepository.count();
            log.info("Total exception count: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting exception count", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test finding all exceptions (limited to 10).
     */
    @GetMapping("/all")
    public ResponseEntity<List<InterfaceException>> getAllExceptions() {
        try {
            List<InterfaceException> exceptions = exceptionRepository.findAll();
            log.info("Found {} exceptions", exceptions.size());

            // Limit to first 10 to avoid large responses
            List<InterfaceException> limited = exceptions.stream()
                    .limit(10)
                    .toList();

            return ResponseEntity.ok(limited);
        } catch (Exception e) {
            log.error("Error getting all exceptions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test finding exception by transaction ID.
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<InterfaceException> getExceptionByTransactionId(@PathVariable String transactionId) {
        try {
            log.info("Searching for exception with transaction ID: {}", transactionId);
            Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);

            if (exceptionOpt.isPresent()) {
                log.info("Found exception for transaction ID: {}", transactionId);
                return ResponseEntity.ok(exceptionOpt.get());
            } else {
                log.info("No exception found for transaction ID: {}", transactionId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error finding exception by transaction ID: {}", transactionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test finding exception by transaction ID with eager loading.
     */
    @GetMapping("/transaction/{transactionId}/eager")
    public ResponseEntity<InterfaceException> getExceptionByTransactionIdEager(@PathVariable String transactionId) {
        try {
            log.info("Searching for exception with eager loading, transaction ID: {}", transactionId);
            Optional<InterfaceException> exceptionOpt = exceptionRepository
                    .findByTransactionIdWithEagerLoading(transactionId);

            if (exceptionOpt.isPresent()) {
                log.info("Found exception with eager loading for transaction ID: {}", transactionId);
                return ResponseEntity.ok(exceptionOpt.get());
            } else {
                log.info("No exception found with eager loading for transaction ID: {}", transactionId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error finding exception with eager loading by transaction ID: {}", transactionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}