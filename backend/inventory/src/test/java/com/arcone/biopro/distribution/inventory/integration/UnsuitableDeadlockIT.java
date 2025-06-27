package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.BioProApplication;
import com.arcone.biopro.distribution.inventory.application.dto.UnsuitableInput;
import com.arcone.biopro.distribution.inventory.application.usecase.UnsuitableUseCase;
import com.arcone.biopro.distribution.inventory.common.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.InventoryUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BioProApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "num.io.threads=1",
        "num.network.threads=1",
        "socket.request.max.bytes=524288000",
        "message.max.bytes=524288000",
        "replica.fetch.max.bytes=524288000"
    }
)
public class UnsuitableDeadlockIT {

    @Autowired
    private UnsuitableUseCase unsuitableUseCase;

    @Autowired
    private InventoryUtil inventoryUtil;

    @Autowired
    private InventoryAggregateRepository repository;

    private final int numThreads = 5;

    private List<String> unitNumbers = new ArrayList<>();

    private static final String UNIT_NUMBER = "W777792312322";
    private static final List<String> PRODUCT_CODES = Arrays.asList("E0678V00", "E0685V00", "E0686V00", "E0869V00", "E1624V00");


    private static final String UNSUITABLE_REASON = "TEST_REASON";

    @BeforeEach
    void setUp() {

        unitNumbers = new ArrayList<>();

        for (var i = 0; i < numThreads; i++) {
            String unitNumber = TestUtil.randomString("W77778", 13);
            Flux.fromIterable(PRODUCT_CODES)
                .flatMap(productCode -> {
                    inventoryUtil.createInventory(unitNumber, productCode, "Family", AboRhType.OP, "1FS","2 Days",InventoryStatus.AVAILABLE, "FROZEN");
                    return Mono.just(productCode);
                })
                .blockLast();
            unitNumbers.add(unitNumber);

        }

        // Create test inventory records
        Flux.fromIterable(PRODUCT_CODES)
            .flatMap(productCode -> {
                inventoryUtil.createInventory(UNIT_NUMBER, productCode, "Family", AboRhType.OP, "1FS","2 Days",InventoryStatus.AVAILABLE, "FROZEN");
                return Mono.just(productCode);
            })
            .blockLast();
    }

    @DisplayName("Should handle concurrent unsuitable operations without deadlock")
    void testConcurrentUnsuitableOperations() throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numThreads);

        // Create concurrent operations
        List<Thread> threads = Stream.iterate(0, i -> i < numThreads, i -> i + 1)
            .map(productCode -> new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    UnsuitableInput input = new UnsuitableInput(UNIT_NUMBER, null, UNSUITABLE_REASON);

                    StepVerifier.create(unsuitableUseCase.execute(input))
                        .expectComplete()
                        .verify(Duration.ofSeconds(10));

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            }))
            .toList();

        // Start all threads simultaneously
        threads.forEach(Thread::start);
        startLatch.countDown();

        // Wait for all operations to complete
        boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue().withFailMessage("Concurrent operations did not complete in time");

        // Verify all products were marked as unsuitable
        Flux<InventoryAggregate> results = Flux.fromIterable(PRODUCT_CODES)
            .flatMap(productCode -> repository.findByUnitNumberAndProductCode(UNIT_NUMBER, productCode));

        StepVerifier.create(results.collectList())
            .assertNext(aggregates -> {
                assertThat(aggregates).hasSize(PRODUCT_CODES.size());
                aggregates.forEach(aggregate -> {
                    assertThat(aggregate.getInventory().getUnsuitableReason()).isEqualTo(UNSUITABLE_REASON);
                });
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent unsuitable operations without deadlock for not same unit number")
    void testConcurrentUnsuitableOperationsForNotSameUnitNumber() throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numThreads);

        // Create concurrent operations
        List<Thread> threads = Stream.iterate(0, i -> i < numThreads, i -> i + 1)
            .map(index -> new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for(String productCode : PRODUCT_CODES) {
                        UnsuitableInput input = new UnsuitableInput(unitNumbers.get(index), productCode, UNSUITABLE_REASON);
                        unsuitableUseCase.execute(input).block();
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            }))
            .toList();

        // Start all threads simultaneously
        threads.forEach(Thread::start);
        startLatch.countDown();

        // Wait for all operations to complete
        boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue().withFailMessage("Concurrent operations did not complete in time");

        // Verify all products were marked as unsuitable
        Flux<InventoryAggregate> results = Flux.fromIterable(unitNumbers)
            .flatMap(unitNumber -> Flux.fromIterable(PRODUCT_CODES)
                .flatMap(productCode -> repository.findByUnitNumberAndProductCode(unitNumber, productCode)));

        StepVerifier.create(results.collectList())
            .assertNext(aggregates -> {
                aggregates.forEach(aggregate -> {
                    assertThat(aggregate.getInventory().getUnsuitableReason()).isEqualTo(UNSUITABLE_REASON);
                });
            })
            .verifyComplete();
    }
}
