package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedCartonItemOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.RecoveredPlasmaShipmentClosedCartonItemOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.RecoveredPlasmaShipmentClosedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.RecoveredPlasmaShipmentClosedOutboundEventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveredPlasmaShipmentClosedOutboundEventMapperTest {

    private RecoveredPlasmaShipmentClosedOutboundEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RecoveredPlasmaShipmentClosedOutboundEventMapper.class);
    }

    @Test
    void modelToCloseEventDTO_ShouldMapAllFields() {
        // Arrange
        RecoveredPlasmaShipmentClosedOutbound source = createSampleShipment();

        // Act
        RecoveredPlasmaShipmentClosedOutboundPayload result = mapper.modelToCloseEventDTO(source);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.shipmentDate()).isEqualTo(source.getShipmentDateFormatted());
        assertThat(result.shipmentCloseDate()).isEqualTo(source.getCloseDateFormatted());
        assertThat(result.totalShipmentProducts()).isEqualTo(source.getTotalShipmentProducts());
        assertThat(result.cartonList()).hasSize(source.getCartonOutboundList().size());
        assertThat(result.shipmentLocationCode()).isEqualTo(source.getShipmentLocationCode());
        assertThat(result.locationShipmentCode()).isEqualTo(source.getLocationShipmentCode());
        assertThat(result.locationCartonCode()).isEqualTo(source.getLocationCartonCode());
        assertThat(result.customerCode()).isEqualTo(source.getCustomerCode());
        assertThat(result.shipmentNumber()).isEqualTo(source.getShipmentNumber());
        assertThat(result.totalShipmentProducts()).isEqualTo(source.getTotalShipmentProducts());

        // carton
        assertThat(result.cartonList().getFirst().cartonNumber()).isEqualTo(source.getCartonOutboundList().getFirst().getCartonNumber());

        // items
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().collectionDate()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getCollectionDateFormatted());
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().isbt128Flag()).isEqualTo("00");
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().unitNumber()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getUnitNumber());
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().productCode()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getProductCode());
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().collectionFacility()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getCollectionFacility());
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().productVolume()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getProductVolume());
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().bloodType()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getBloodType());
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().collectionTimeZone()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getCollectionTimeZone());
        assertThat(result.cartonList().getFirst().packedProducts().getFirst().collectionDate()).isEqualTo(source.getCartonOutboundList().getFirst().getPackedProducts().getFirst().getCollectionDateFormatted());
    }

    @Test
    void mapClosedProducts_WithNullInput_ShouldReturnNull() {
        // Act
        List<RecoveredPlasmaShipmentClosedCartonItemOutboundPayload> result = mapper.mapClosedProducts(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void productModelToEventDTO_ShouldMapAllFields() {
        // Arrange
        RecoveredPlasmaShipmentClosedCartonItemOutbound source = createSampleCartonItem();

        // Act
        RecoveredPlasmaShipmentClosedCartonItemOutboundPayload result = mapper.productModelToEventDTO(source);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.collectionDate()).isEqualTo(source.getCollectionDateFormatted());
        assertThat(result.isbt128Flag()).isEqualTo("00");
    }

    private RecoveredPlasmaShipmentClosedOutbound createSampleShipment() {
        ///String shipmentNumber, String locationShipmentCode, String locationCartonCode, String customerCode, LocalDate shipmentDate , ZonedDateTime closeDate, String shipmentLocationCode
        RecoveredPlasmaShipmentClosedOutbound shipment = new RecoveredPlasmaShipmentClosedOutbound("NUMBER","LOCATION_CODE"
            ,"LOCATION_CARTON_CODE","CUSTOMER_CODE", LocalDate.now(), ZonedDateTime.now(),"SHIPMENT_LOCATION_CODE");

        shipment.addCarton("CARTON_NUMBER",10,List.of(createSampleCartonItem()));
        return shipment;
    }

    private RecoveredPlasmaShipmentClosedCartonItemOutbound createSampleCartonItem() {

        String unitNumber = "UN123";
        String productCode = "PC456";
        String collectionFacility = "CF789";
        ZonedDateTime collectionDate = ZonedDateTime.parse("2025-05-30T17:06:35.691463Z");
        Integer productVolume = 255;
        String bloodType = "A+";
        String collectionTimeZone = "America/New_York";


        return
            new RecoveredPlasmaShipmentClosedCartonItemOutbound(
                unitNumber,
                productCode,
                collectionFacility,
                collectionDate,
                productVolume,
                bloodType,
                collectionTimeZone
            );
    }
}

