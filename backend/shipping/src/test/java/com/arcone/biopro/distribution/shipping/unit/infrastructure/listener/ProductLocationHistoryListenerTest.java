package com.arcone.biopro.distribution.shipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedItemPayloadDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedItemProductPayloadDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedPayloadDTO;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.CreateExternalTransferCommand;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.ProductLocationHistoryListener;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ProductLocationHistoryMapper;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ProductLocationHistoryEntity;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ProductLocationHistoryEntityRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;

class ProductLocationHistoryListenerTest {

    private ProductLocationHistoryListener productLocationHistoryListener;
    private ProductLocationHistoryEntityRepository productLocationHistoryEntityRepository;
    private ProductLocationHistoryMapper productLocationHistoryMapper;
    private CustomerService customerService;

    @Test
    public void shouldHandleShipmentCompletedEvents(){

        customerService = Mockito.mock(CustomerService.class);
        productLocationHistoryEntityRepository = Mockito.mock(ProductLocationHistoryEntityRepository.class);
        productLocationHistoryMapper = new ProductLocationHistoryMapper(customerService);
        productLocationHistoryListener = new ProductLocationHistoryListener(productLocationHistoryEntityRepository, productLocationHistoryMapper);

        Mockito.when(productLocationHistoryEntityRepository.save(Mockito.any())).thenReturn(Mono.just(ProductLocationHistoryEntity.builder().build()));

        var event = Mockito.mock(ShipmentCompletedEvent.class);
        Mockito.when(event.getPayload()).thenReturn(ShipmentCompletedPayloadDTO
            .builder()
                .lineItems(List.of(ShipmentCompletedItemPayloadDTO
                    .builder()
                        .products(List.of(ShipmentCompletedItemProductPayloadDTO
                            .builder()
                                .unitNumber("UNIT")
                                .productCode("PRODUCT_CODE")
                            .build()))
                    .build()))
            .build());

        productLocationHistoryListener.handleShipmentCompletedEvent(event);

        Mockito.verify(productLocationHistoryEntityRepository).save(Mockito.any(ProductLocationHistoryEntity.class));

    }

}
