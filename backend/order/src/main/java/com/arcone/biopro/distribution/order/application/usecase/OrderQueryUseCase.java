package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.exception.QueryDidNotReturnAnyResultsException;
import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.repository.OrderQueryRepository;
import com.arcone.biopro.distribution.order.domain.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class OrderQueryUseCase implements OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    @Override
    public Flux<OrderReport> searchOrders(OrderQueryCommand orderQueryCommand) {
        return orderQueryRepository.searchOrders(orderQueryCommand)
            .switchIfEmpty(Flux.error(QueryDidNotReturnAnyResultsException::new));
    }
}
