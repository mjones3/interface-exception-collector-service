package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.application.mapper.ModifyOrderReceivedEventMapper;
import com.arcone.biopro.distribution.order.domain.event.OrderModifiedEvent;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.vo.ModifyByProcess;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.ModifyOrderService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModifyOrderUseCase extends AbstractProcessOrderUseCase implements ModifyOrderService {

    private final OrderRepository orderRepository;
    private final ModifyOrderReceivedEventMapper modifyOrderReceivedEventMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CustomerService customerService;
    private final LookupService lookupService;
    private final OrderConfigService orderConfigService;
    private final static String USE_CASE_OPERATION = "MODIFY_ORDER";


    @Override
    public Mono<Void> processModifyOrderEvent(ModifyOrderReceivedDTO modifyOrderReceivedDTO) {
        return this.orderRepository.findByExternalId(modifyOrderReceivedDTO.payload().externalId())
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s", modifyOrderReceivedDTO.payload().externalId()))))
            .collectList()
            .flatMap(orderList -> {
                var orderModified = orderList.getFirst().modify(modifyOrderReceivedEventMapper.mapToCommand(modifyOrderReceivedDTO.payload(), ModifyByProcess.INTERFACE),orderList
                    , customerService , lookupService , orderConfigService);
                return this.orderRepository.reset(orderModified)
                    .doOnSuccess(this::publishOrderProcessedEvent);
            })
            .then()
            .onErrorResume(error -> {
                    log.error("Not able to process order modified event {}",error.getMessage());
                    this.publishOrderRejectedEvent(applicationEventPublisher,modifyOrderReceivedDTO.payload().externalId(), error , USE_CASE_OPERATION);
                    return Mono.empty();
                }
            );
    }

    @Override
    void publishOrderProcessedEvent(Order order) {
        log.debug("Publishing OrderModifiedEvent {} ", order );
        applicationEventPublisher.publishEvent(new OrderModifiedEvent(order));
    }
}
