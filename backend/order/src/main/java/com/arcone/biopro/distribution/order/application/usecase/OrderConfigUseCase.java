package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.domain.repository.OrderConfigRepository;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrderConfigUseCase implements OrderConfigService {
    private final OrderConfigRepository orderConfigRepository;

    private static final String BACK_ORDER_CREATION_CONFIG_TYPE = "BACK_ORDER_CREATION";

    public OrderConfigUseCase(OrderConfigRepository orderConfigRepository) {
        this.orderConfigRepository = orderConfigRepository;
    }

    @Override
    public Mono<String> findProductFamilyByCategory(String productCategory, String productFamily) {
        return orderConfigRepository.findProductFamilyByCategory(productCategory, productFamily);
    }

    @Override
    public Mono<String> findBloodTypeByFamilyAndType(String productFamily, String bloodType) {
        return orderConfigRepository.findBloodTypeByFamilyAndType(productFamily, bloodType);
    }

    @Override
    public Mono<Boolean> findBackOrderConfiguration() {
        return orderConfigRepository.findFirstConfigAsBoolean(BACK_ORDER_CREATION_CONFIG_TYPE);
    }
}
