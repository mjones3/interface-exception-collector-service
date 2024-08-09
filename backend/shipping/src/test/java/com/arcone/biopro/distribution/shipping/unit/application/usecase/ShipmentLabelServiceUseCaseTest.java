package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.component.BarcodeGenerator;
import com.arcone.biopro.distribution.shipping.application.usecase.ShipmentLabelServiceUseCase;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.service.FacilityServiceMock;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.FacilityDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;


class ShipmentLabelServiceUseCaseTest {

    private ShipmentRepository shipmentRepository;
    private BarcodeGenerator barcodeGenerator;
    private FacilityServiceMock facilityServiceMock;
    private ShipmentItemRepository shipmentItemRepository;

    private ShipmentItemPackedRepository shipmentItemPackedRepository;


    @BeforeEach
    public void setUp(){

        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        barcodeGenerator = Mockito.mock(BarcodeGenerator.class);
        facilityServiceMock = Mockito.mock(FacilityServiceMock.class);
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        shipmentItemPackedRepository = Mockito.mock(ShipmentItemPackedRepository.class);
    }

    @Test
    public void shouldNotGeneratePackingListWhenShipmentIsOpen(){
        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        var target = new ShipmentLabelServiceUseCase(shipmentRepository,barcodeGenerator,facilityServiceMock,shipmentItemRepository,shipmentItemPackedRepository);

        var label = target.generatePackingListLabel(1L);

        StepVerifier
            .create(label)
            .expectError()
            .verify();
    }

    @Test
    public void shouldNotGeneratePackingListWhenShipmentDoesNotExist(){

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.empty());

        var target = new ShipmentLabelServiceUseCase(shipmentRepository,barcodeGenerator,facilityServiceMock,shipmentItemRepository,shipmentItemPackedRepository);

        var label = target.generatePackingListLabel(1L);

        StepVerifier
            .create(label)
            .expectError()
            .verify();
    }

    @Test
    public void shouldGeneratePackingList(){

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getLocationCode()).thenReturn("MDL_HUB_1");
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.COMPLETED);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getCustomerCode()).thenReturn(35L);
        Mockito.when(shipment.getCustomerName()).thenReturn("customer_name");
        Mockito.when(shipment.getAddressLine1()).thenReturn("customer_address_line_1");
        Mockito.when(shipment.getAddressLine2()).thenReturn("customer_address_line_2");
        Mockito.when(shipment.getCity()).thenReturn("customer_city");
        Mockito.when(shipment.getState()).thenReturn("customer_state");
        Mockito.when(shipment.getPostalCode()).thenReturn("customer_postal_code");
        Mockito.when(shipment.getCreatedByEmployeeId()).thenReturn("created_employ_id");
        Mockito.when(shipment.getCompletedByEmployeeId()).thenReturn("complete_employ_id");
        Mockito.when(shipment.getCompleteDate()).thenReturn(ZonedDateTime.now());

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));


        ShipmentItem item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("product_family");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);

        Mockito.when(shipmentItemRepository.findAllByShipmentId(1L)).thenReturn(Flux.just(item));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(1L)).thenReturn(Flux.just(ShipmentItemPacked.builder()
                .aboRh("AP")
                .collectionDate(ZonedDateTime.now())
                .expirationDate(ZonedDateTime.now())
                .packedByEmployeeId("test")
                .shipmentItemId(1L)
                .unitNumber("UN")
                .productCode("product_code")
                .productDescription("product_description")
            .build()));

        Mockito.when(facilityServiceMock.getFacilityId("MDL_HUB_1")).thenReturn(Mono.just(FacilityDTO.builder()
                .name("Facility Name")
                .externalId("IC 39")
                .addressLine1("Address Line 1")
                .addressLine2("Address Line 2")
                .city("city")
                .state("state")
                .postalCode("postal_code")
            .build()));


        var target = new ShipmentLabelServiceUseCase(shipmentRepository,barcodeGenerator,facilityServiceMock,shipmentItemRepository,shipmentItemPackedRepository);

        var label = target.generatePackingListLabel(1L);

        StepVerifier
            .create(label)
            .consumeNextWith(detail -> {
                Assertions.assertEquals(Optional.of(56L), Optional.of(detail.orderNumber()));
                Assertions.assertEquals(Optional.of("complete_employ_id"), Optional.of(detail.packedBy()));
                Assertions.assertEquals(Optional.of("created_employ_id"), Optional.of(detail.enteredBy()));
                Assertions.assertNotNull(detail.dateTimePacked());
                Assertions.assertNotNull(detail.orderIdBase64Barcode());
                Assertions.assertNotNull(detail.shipmentIdBase64Barcode());
                Assertions.assertEquals(Optional.of("AP"), Optional.of(detail.packedItems().get(0).aboRh()));
                Assertions.assertEquals(Optional.of("UN"), Optional.of(detail.packedItems().get(0).unitNumber()));
                Assertions.assertEquals(Optional.of("product_code"), Optional.of(detail.packedItems().get(0).productCode()));
                Assertions.assertEquals(Optional.of("product_description"), Optional.of(detail.packedItems().get(0).productDescription()));
                Assertions.assertEquals(Optional.of("product_family"), Optional.of(detail.packedItems().get(0).productFamily()));
                Assertions.assertNotNull(detail.packedItems().get(0).expirationDate());
                Assertions.assertNotNull(detail.packedItems().get(0).collectionDate());
                Assertions.assertNotNull(detail.shipFrom());
                Assertions.assertEquals(Optional.of("IC 39"), Optional.of(detail.shipFrom().bloodCenterCode()));
                Assertions.assertEquals(Optional.of("Facility Name"), Optional.of(detail.shipFrom().bloodCenterName()));
                Assertions.assertEquals(Optional.of("Address Line 1"), Optional.of(detail.shipFrom().bloodCenterAddressLine1()));
                Assertions.assertEquals(Optional.of("Address Line 2"), Optional.of(detail.shipFrom().bloodCenterAddressLine2()));
                Assertions.assertEquals(Optional.of("city, state, postal_code"), Optional.of(detail.shipFrom().bloodCenterAddressComplement()));
                Assertions.assertNotNull(detail.shipTo());
                Assertions.assertEquals(Optional.of(35L), Optional.of(detail.shipTo().customerCode()));
                Assertions.assertEquals(Optional.of("customer_name"), Optional.of(detail.shipTo().customerName()));
                Assertions.assertEquals(Optional.of("customer_address_line_1"), Optional.of(detail.shipTo().addressLine1()));
                Assertions.assertEquals(Optional.of("customer_address_line_2"), Optional.of(detail.shipTo().addressLine2()));
                Assertions.assertEquals(Optional.of("customer_city, customer_state, customer_postal_code"), Optional.of(detail.shipTo().addressComplement()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldGenerateShippingLabel(){

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getLocationCode()).thenReturn("MDL_HUB_1");
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.COMPLETED);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getCustomerCode()).thenReturn(35L);
        Mockito.when(shipment.getCustomerName()).thenReturn("customer_name");
        Mockito.when(shipment.getAddressLine1()).thenReturn("customer_address_line_1");
        Mockito.when(shipment.getAddressLine2()).thenReturn("customer_address_line_2");
        Mockito.when(shipment.getCity()).thenReturn("customer_city");
        Mockito.when(shipment.getState()).thenReturn("customer_state");
        Mockito.when(shipment.getPostalCode()).thenReturn("customer_postal_code");
        Mockito.when(shipment.getCreatedByEmployeeId()).thenReturn("created_employ_id");
        Mockito.when(shipment.getCompletedByEmployeeId()).thenReturn("complete_employ_id");
        Mockito.when(shipment.getCustomerPhoneNumber()).thenReturn("123456-88");
        Mockito.when(shipment.getCompleteDate()).thenReturn(ZonedDateTime.now());

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        Mockito.when(facilityServiceMock.getFacilityId("MDL_HUB_1")).thenReturn(Mono.just(FacilityDTO.builder()
            .name("Facility Name")
            .externalId("IC 39")
            .addressLine1("Address Line 1")
            .addressLine2("Address Line 2")
            .city("city")
            .state("state")
            .postalCode("postal_code")
                .properties(Map.of("PHONE_NUMBER","(704) 972-4742"))
            .build()));


        var target = new ShipmentLabelServiceUseCase(shipmentRepository,barcodeGenerator,facilityServiceMock,shipmentItemRepository,shipmentItemPackedRepository);

        var label = target.generateShippingLabel(1L);

        StepVerifier
            .create(label)
            .consumeNextWith(detail -> {
                Assertions.assertEquals(Optional.of(56L), Optional.of(detail.orderNumber()));
                Assertions.assertNotNull(detail.dateTimePacked());
                Assertions.assertNotNull(detail.orderIdBase64Barcode());
                Assertions.assertNotNull(detail.shipmentIdBase64Barcode());
                Assertions.assertNotNull(detail.shipFrom());
                Assertions.assertNotNull(detail.shipFrom().bloodCenterBase64Barcode());
                Assertions.assertEquals(Optional.of("IC 39"), Optional.of(detail.shipFrom().bloodCenterCode()));
                Assertions.assertEquals(Optional.of("Facility Name"), Optional.of(detail.shipFrom().bloodCenterName()));
                Assertions.assertEquals(Optional.of("Address Line 1"), Optional.of(detail.shipFrom().bloodCenterAddressLine1()));
                Assertions.assertEquals(Optional.of("Address Line 2"), Optional.of(detail.shipFrom().bloodCenterAddressLine2()));
                Assertions.assertEquals(Optional.of("city, state, postal_code"), Optional.of(detail.shipFrom().bloodCenterAddressComplement()));
                Assertions.assertEquals(Optional.of("(704) 972-4742"), Optional.of(detail.shipFrom().phoneNumber()));
                Assertions.assertNotNull(detail.shipTo());
                Assertions.assertEquals(Optional.of(35L), Optional.of(detail.shipTo().customerCode()));
                Assertions.assertEquals(Optional.of("customer_name"), Optional.of(detail.shipTo().customerName()));
                Assertions.assertEquals(Optional.of("customer_address_line_1"), Optional.of(detail.shipTo().addressLine1()));
                Assertions.assertEquals(Optional.of("customer_address_line_2"), Optional.of(detail.shipTo().addressLine2()));
                Assertions.assertEquals(Optional.of("customer_city, customer_state, customer_postal_code"), Optional.of(detail.shipTo().addressComplement()));
                Assertions.assertEquals(Optional.of("123456-88"), Optional.of(detail.shipTo().phoneNumber()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGenerateShippingLabelWhenShipmentIsOpen(){
        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        var target = new ShipmentLabelServiceUseCase(shipmentRepository,barcodeGenerator,facilityServiceMock,shipmentItemRepository,shipmentItemPackedRepository);

        var label = target.generateShippingLabel(1L);

        StepVerifier
            .create(label)
            .expectError()
            .verify();
    }

    @Test
    public void shouldNotGenerateShippingLabelWhenShipmentDoesNotExist(){

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.empty());

        var target = new ShipmentLabelServiceUseCase(shipmentRepository,barcodeGenerator,facilityServiceMock,shipmentItemRepository,shipmentItemPackedRepository);

        var label = target.generatePackingListLabel(1L);

        StepVerifier
            .create(label)
            .expectError()
            .verify();
    }

}
