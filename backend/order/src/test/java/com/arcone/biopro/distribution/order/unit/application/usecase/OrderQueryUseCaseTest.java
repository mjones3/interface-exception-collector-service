package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.order.application.usecase.OrderQueryUseCase;
import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.Page;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomUtils.nextInt;

@SpringJUnitConfig(classes = { OrderQueryUseCase.class })
class OrderQueryUseCaseTest {

    @MockBean
    OrderRepository orderRepository;
    @Autowired
    OrderQueryUseCase orderQueryUseCase;

    @Test
    void shouldSearchIfMinimalValuesFound() {
        var locationCode = "99999999";
        var totalRecords = 50;
        var command = new OrderQueryCommand(locationCode, null, null, null, null, null, null, null, null, null, null, null,null);
        var contents = createContents(command.getPageSize());

        given(orderRepository.search(eq(command)))
            .willReturn(Mono.just(
                new Page<>(
                    Arrays.asList(contents),
                    command.getPageNumber(),
                    command.getPageSize(),
                    totalRecords,
                    command.getQuerySort()
                )
            ));

        // Act
        var result = StepVerifier.create(orderQueryUseCase.search(command));

        // Assert
        result.expectNextMatches(page ->
                page.getContent().size() == contents.length
                    && page.getPageNumber() == command.getPageNumber()
                    && page.getPageSize() == command.getPageSize()
                    && page.getTotalRecords() == totalRecords
                    && Objects.equals(
                        ofNullable(page.getQuerySort())
                            .map(QuerySort::getQueryOrderByList)
                            .map(List::size).orElse(null),
                        ofNullable(command.getQuerySort())
                            .map(QuerySort::getQueryOrderByList)
                            .map(List::size).orElse(null)
                    )
            )
            .expectComplete()
            .verify();
    }

    @Test
    void shouldThrowExceptionWhenNoResultsFound() {
        var locationCode = "99999999";
        var command = new OrderQueryCommand(locationCode, null, null, null, null, null, null, null, null, null, null, null,null);

        given(orderRepository.search(eq(command)))
            .willReturn(Mono.just(
                new Page<>(
                    Collections.emptyList(),
                    command.getPageNumber(),
                    command.getPageSize(),
                    0,
                    command.getQuerySort()
                )
            ));

        // Act
        var result = StepVerifier.create(orderQueryUseCase.search(command));

        // Assert
        result.expectError(NoResultsFoundException.class).verify();
    }

    private OrderReport[] createContents(int quantity) {
        OrderReport[] contents = new OrderReport[quantity];
        for (int i = 0; i < quantity; i++) {
            contents[i] = createContent((long) i);
        }
        return contents;
    }

    private OrderReport createContent(Long id) {
        return new OrderReport(
            id,
            id,
            id.toString(),
            randomAlphanumeric(16),
            new OrderCustomerReport(randomAlphanumeric(16), randomAlphanumeric(16)),
            new OrderPriorityReport(randomAlphanumeric(16), randomAlphanumeric(16)),
            ZonedDateTime.now(),
            LocalDate.now().plusDays(nextInt(0, 10)),"INTERNAL_TRANSFER"
        );
    }

}
