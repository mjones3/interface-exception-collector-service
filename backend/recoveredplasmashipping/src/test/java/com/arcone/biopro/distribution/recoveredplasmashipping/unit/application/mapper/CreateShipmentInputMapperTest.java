package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateShipmentInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CreateShipmentInputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateShipmentCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
class CreateShipmentInputMapperTest {

    private CreateShipmentInputMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CreateShipmentInputMapper.class);
    }

    @Test
    @DisplayName("Should map all fields correctly from input to command")
    void shouldMapAllFieldsCorrectly() {
        // Arrange
        LocalDate scheduleDate = LocalDate.now();
        var input = CreateShipmentInput.builder()
            .locationCode("LOC001")
            .customerCode("CUST001")
            .productType("PLASMA_TYPE_A")
            .transportationReferenceNumber("TRN001")
            .shipmentDate(scheduleDate)
            .cartonTareWeight(BigDecimal.valueOf(15.5))
            .createEmployeeId("EMP001")
            .build();

        // Act
        CreateShipmentCommand result = mapper.toCreateCommand(input);

        // Assert
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getLocationCode()).isEqualTo(input.locationCode()),
            () -> assertThat(result.getCustomerCode()).isEqualTo(input.customerCode()),
            () -> assertThat(result.getProductType()).isEqualTo(input.productType()),
            () -> assertThat(result.getTransportationReferenceNumber()).isEqualTo(input.transportationReferenceNumber()),
            () -> assertThat(result.getShipmentDate()).isEqualTo(input.shipmentDate()),
            () -> assertThat(result.getCartonTareWeight()).isEqualTo(input.cartonTareWeight()),
            () -> assertThat(result.getCreateEmployeeId()).isEqualTo(input.createEmployeeId())
        );
    }

    @Test
    void shouldHandleNullValuesOrEmptyValues() {
        // Arrange
        var input = CreateShipmentInput.builder()
            .locationCode(null)
            .build();

        try {
            mapper.toCreateCommand(input);
            Assertions.fail();
        }catch (IllegalArgumentException e){
            assertThat(e.getMessage()).isEqualTo("Customer code is required");
        }
    }
}

