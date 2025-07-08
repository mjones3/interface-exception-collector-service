package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.OrderQueryController;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderQueryCommandDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.order.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.order.application.mapper.OrderQueryMapper;
import com.arcone.biopro.distribution.order.application.mapper.OrderReportMapper;
import com.arcone.biopro.distribution.order.application.mapper.PageMapper;
import com.arcone.biopro.distribution.order.application.mapper.QuerySortMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.Page;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import com.arcone.biopro.distribution.order.domain.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomUtils.nextInt;

@SpringJUnitConfig(classes = { OrderQueryController.class, OrderQueryMapper.class, OrderReportMapper.class, QuerySortMapper.class, PageMapper.class })
class OrderQueryControllerTest {

    @MockBean
    OrderQueryService orderQueryService;
    @Autowired
    OrderQueryMapper orderQueryMapper;
    @Autowired
    OrderQueryController orderQueryController;

    @Test
    void shouldSearchOrders() {
        // Arrange
        var locationCode = "99999999";
        var pageSize = 20;
        var totalRecords = 50;
        var command = OrderQueryCommandDTO.builder()
            .locationCode(locationCode)
            .build();
        var contents = createContents(pageSize);
        var page = new Page<>(
            Arrays.asList(contents),
            0,
            pageSize,
            totalRecords,
            null
        );

        given(this.orderQueryService.search(eq(orderQueryMapper.mapToDomain(command))))
            .willReturn(Mono.just(page));

        // Act
        var result = StepVerifier.create(orderQueryController.searchOrders(command));

        // Assert
        result.expectNextMatches(pageDTO ->
                pageDTO.content().size() == page.getContent().size()
                && pageDTO.pageNumber() == page.getPageNumber()
                && pageDTO.pageSize() == page.getPageSize()
                && pageDTO.totalRecords() == page.getTotalRecords()
                && Objects.equals(
                    ofNullable(pageDTO.querySort())
                        .map(QuerySortDTO::orderByList)
                        .map(List::size).orElse(null),
                    ofNullable(page.getQuerySort())
                        .map(QuerySort::getQueryOrderByList)
                        .map(List::size).orElse(null)
                )
            )
            .expectComplete()
            .verify();
    }

    @CsvSource({
        "0,20",
        "1,20",
        "2,10"
    })
    @ParameterizedTest
    void shouldSearchOrdersForSpecifiedPage(int pageNumber, int amountOfRecords) {
        // Arrange
        var locationCode = "99999999";
        var pageSize = 20;
        var totalRecords = 50;
        var command = OrderQueryCommandDTO.builder()
            .locationCode(locationCode)
            .pageSize(pageSize)
            .pageNumber(pageNumber)
            .build();

        var contents = createContents(amountOfRecords);
        var page = new Page<>(
            Arrays.asList(contents),
            pageNumber,
            pageSize,
            totalRecords,
            null
        );

        given(this.orderQueryService.search(eq(orderQueryMapper.mapToDomain(command))))
            .willReturn(Mono.just(page));

        // Act
        var result = StepVerifier.create(orderQueryController.searchOrders(command));

        // Assert
        result.expectNextMatches(pageDTO ->
                pageDTO.content().size() == page.getContent().size()
                    && pageDTO.pageNumber() == page.getPageNumber()
                    && pageDTO.pageSize() == page.getPageSize()
                    && pageDTO.totalRecords() == page.getTotalRecords()
                    && Objects.equals(
                        ofNullable(pageDTO.querySort())
                            .map(QuerySortDTO::orderByList)
                            .map(List::size).orElse(null),
                        ofNullable(page.getQuerySort())
                            .map(QuerySort::getQueryOrderByList)
                            .map(List::size).orElse(null)
                    )
            )
            .expectComplete()
            .verify();
    }

    @Test
    void shouldNotSearchOrdersWhenNoResultsFound() {
        // Arrange
        var locationCode = "99999999";
        var command = OrderQueryCommandDTO.builder()
            .locationCode(locationCode)
            .build();

        given(this.orderQueryService.search(eq(orderQueryMapper.mapToDomain(command))))
            .willReturn(Mono.error(NoResultsFoundException::new));

        // Act
        var result = StepVerifier.create(orderQueryController.searchOrders(command));

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
