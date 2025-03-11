package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.Page;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderQueryUseCase implements OrderQueryService {

    private final OrderRepository orderRepository;

    @Override
    public Mono<Page<OrderReport>> search(OrderQueryCommand orderQueryCommand) {
        return orderRepository.search(orderQueryCommand)
            .filter(p -> p != null && p.getContent() != null && !p.getContent().isEmpty())
            .switchIfEmpty(Mono.error(NoResultsFoundException::new));
    }

}
