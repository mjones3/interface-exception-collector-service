package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.PickListController;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListDTO;
import com.arcone.biopro.distribution.order.application.mapper.PickListMapper;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.service.PickListService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig(classes = { PickListController.class, PickListMapper.class, PickListService.class })
class PickListControllerTest {

    @Autowired
    PickListController pickListController;

    @MockBean
    PickListMapper pickListMapper;

    @MockBean
    PickListService pickListService;

    @Test
    public void shouldGeneratePickList(){

        var pickList = Mockito.mock(PickList.class);

        Mockito.when(pickListService.generatePickList(Mockito.any())).thenReturn(Mono.just(pickList));
        Mockito.when(pickListMapper.mapToDTO(Mockito.any())).thenReturn(PickListDTO
            .builder()
                .orderNumber(1L)
            .build());

        StepVerifier.create(pickListController.generatePickList(1L))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.orderNumber());

                }
            )
            .verifyComplete();
    }
}
