package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.PickListController;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListResponseDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
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
        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);
        Mockito.when(useCaseResponse.data()).thenReturn(pickList);

        var dto = Mockito.mock(PickListResponseDTO.class);

        var pickListDto = Mockito.mock(PickListDTO.class);
        Mockito.when(pickListDto.orderNumber()).thenReturn(1L);

        Mockito.when(dto.data()).thenReturn(pickListDto);

        Mockito.when(pickListMapper.mapToDTO(Mockito.any())).thenReturn(dto);

        Mockito.when(pickListService.generatePickList(Mockito.anyLong(),Mockito.anyBoolean())).thenReturn(Mono.just(useCaseResponse));

        StepVerifier.create(pickListController.generatePickList(1L, Boolean.FALSE))
            .consumeNextWith(detail -> {
                    Assertions.assertEquals(1L,  detail.data().orderNumber());

                }
            )
            .verifyComplete();
    }
}
