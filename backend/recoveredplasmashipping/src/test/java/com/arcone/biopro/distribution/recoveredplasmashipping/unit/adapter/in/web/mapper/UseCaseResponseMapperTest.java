package com.arcone.biopro.distribution.recoveredplasmashipping.unit.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CustomerDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CartonDtoMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CustomerDtoMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.RecoveredPlasmaShipmentDtoMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseNotificationDtoMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(classes = {UseCaseResponseMapperImpl.class, UseCaseNotificationDtoMapperImpl.class, CustomerDtoMapperImpl.class , RecoveredPlasmaShipmentDtoMapperImpl.class , CartonDtoMapperImpl.class})
class UseCaseResponseMapperTest {

    @Autowired
    private UseCaseResponseMapper mapper;



    @Test
    void shouldMapToUseCaseOutputCustomerDto() {

        UseCaseOutput<CustomerOutput> useCaseOutput =  new UseCaseOutput<>(List.of(UseCaseNotificationOutput
            .builder()
            .useCaseMessage(UseCaseMessage.builder()
                .type(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getType())
                .message(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getMessage())
                .build())
            .build())
            , CustomerOutput.builder().code("123").build()
            , Map.of("next", "test"));


        // Act
        UseCaseResponseDTO<CustomerDTO> result = mapper.toUseCaseResponseDTO(useCaseOutput);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals("SUCCESS",result.notifications().getFirst().type());
        Assertions.assertEquals("Shipment created successfully",result.notifications().getFirst().message());
        Assertions.assertEquals("123",result.data().code());
        assertNotNull(result._links());

    }

    @Test
    void shouldMapToUseCaseOutputShipmentResponseDto() {

        UseCaseOutput<RecoveredPlasmaShipmentOutput> useCaseOutput =  new UseCaseOutput<>(List.of(UseCaseNotificationOutput
            .builder()
            .useCaseMessage(
                UseCaseMessage.builder()
                    .type(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getType())
                    .message(UseCaseMessageType.SHIPMENT_CREATED_SUCCESS.getMessage())
                    .build()
                )
            .build())
            , RecoveredPlasmaShipmentOutput.builder().locationCode("123").build()
            , Map.of("next", "test"));


        // Act
        UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> result = mapper.toUseCaseRecoveredPlasmaShipmentResponseDTO(useCaseOutput);

        // Assert
        assertNotNull(result);
        Assertions.assertEquals("SUCCESS",result.notifications().getFirst().type());
        Assertions.assertEquals("Shipment created successfully",result.notifications().getFirst().message());
        Assertions.assertEquals("123",result.data().locationCode());
        assertNotNull(result._links());

    }
}

