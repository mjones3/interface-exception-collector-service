package com.arcone.biopro.distribution.shipping.unit.domain.model;

import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.model.ProductLocationHistory;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ExternalTransferStatus;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import com.arcone.biopro.distribution.shipping.domain.repository.ProductLocationHistoryRepository;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;

class ExternalTransferTest {

    private ProductLocationHistoryRepository productLocationHistoryRepository;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        productLocationHistoryRepository = Mockito.mock(ProductLocationHistoryRepository.class);
        customerService = Mockito.mock(CustomerService.class);
        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO
            .builder()
            .code("code")
            .name("Name")
            .build()));
    }

    @Test
    void shouldCreate() {

        var response = new ExternalTransfer(1L, "123", null, "A123", LocalDate.now().minusDays(2), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);

        Assertions.assertNotNull(response);
    }

    @Test
    void shouldNotCreate() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, null, null, "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING , ZonedDateTime.now(),customerService), "Customer cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "A123", "123", "A123", null, "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService), "Transfer date cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "A123", "123", "A123", LocalDate.now().plusDays(2), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService), "Transfer date cannot be in the future");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "123", null, "A123", LocalDate.now().minusDays(2), null, ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService), "Employee ID cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "123", null, "A123", LocalDate.now(), "employee-id", null, ZonedDateTime.now(),customerService),"Status cannot be null");

        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.empty());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransfer(1L, "123", null, "A123", LocalDate.now(), "employee-id",  ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService),"Customer To should be valid");

    }

    @Test
    public void shouldNotAddItemWhenLastShipDateIsAfterTransferDate() {

        var externalTransfer = new ExternalTransfer(1L, "456", null, "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);


        var locationHistory = Mockito.mock(ProductLocationHistory.class);
        Mockito.when(locationHistory.getCreatedDate()).thenReturn(ZonedDateTime.now().plusDays(2));

        var customerTo = Mockito.mock(Customer.class);
        Mockito.when(customerTo.getCode()).thenReturn("123");

        Mockito.when(locationHistory.getCustomerTo()).thenReturn(customerTo);

        Mockito.when(productLocationHistoryRepository.findCurrentLocation(Mockito.any())).thenReturn(Mono.just(locationHistory));

        try {
            externalTransfer.addItem(null,"unitNumber","productCode","employee-id",productLocationHistoryRepository);
            Assertions.fail();
        }catch (DomainException e) {
            Assertions.assertEquals("The transfer date is before the last shipped date",e.getUseCaseMessageType().getMessage());
        }


    }

    @Test
    public void shouldNotAddItemWhenLastShipLocationDoesNotMatch() {

        var externalTransfer = new ExternalTransfer(1L, "567", "567", "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);


        var locationHistory = Mockito.mock(ProductLocationHistory.class);
        Mockito.when(locationHistory.getCreatedDate()).thenReturn(ZonedDateTime.now().minusDays(2));

        var customerTo = Mockito.mock(Customer.class);
        Mockito.when(customerTo.getCode()).thenReturn("123");

        Mockito.when(locationHistory.getCustomerTo()).thenReturn(customerTo);

        Mockito.when(productLocationHistoryRepository.findCurrentLocation(Mockito.any())).thenReturn(Mono.just(locationHistory));

        try {
            externalTransfer.addItem(null,"unitNumber","productCode","employee-id",productLocationHistoryRepository);
            Assertions.fail();
        }catch (DomainException e) {
            Assertions.assertEquals("The product location doesn't match the last shipped location",e.getUseCaseMessageType().getMessage());
        }


    }

    @Test
    public void shouldNotAddItemWhenProductIsNotShipped() {

        var externalTransfer = new ExternalTransfer(1L, "123", "567", "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);

        Mockito.when(productLocationHistoryRepository.findCurrentLocation(Mockito.any())).thenReturn(Mono.empty());

        try {
            externalTransfer.addItem(null,"unitNumber","productCode","employee-id",productLocationHistoryRepository);
            Assertions.fail();
        }catch (DomainException e) {
            Assertions.assertEquals("This product has not been shipped",e.getUseCaseMessageType().getMessage());
        }
    }

    @Test
    public void shouldNotAddItemWhenCurrentLocationIsSameAsTransferToLocation() {

        var externalTransfer = new ExternalTransfer(1L, "123", "567", "A123", LocalDate.now().minusDays(1), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);


        var locationHistory = Mockito.mock(ProductLocationHistory.class);
        Mockito.when(locationHistory.getCreatedDate()).thenReturn(ZonedDateTime.now().minusDays(2));

        var customerTo = Mockito.mock(Customer.class);
        Mockito.when(customerTo.getCode()).thenReturn("123");

        Mockito.when(locationHistory.getCustomerTo()).thenReturn(customerTo);

        Mockito.when(productLocationHistoryRepository.findCurrentLocation(Mockito.any())).thenReturn(Mono.just(locationHistory));

        try {
            externalTransfer.addItem(null,"unitNumber","productCode","employee-id",productLocationHistoryRepository);
            Assertions.fail();
        }catch (DomainException e) {
            Assertions.assertEquals("Last Shipped Location cannot be same as the Transfer to Customer location",e.getUseCaseMessageType().getMessage());
        }

    }


    @Test
    public void shouldAddItem() {

        var externalTransfer = new ExternalTransfer(1L, "456", null, "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);

        Assertions.assertNull(externalTransfer.getCustomerFrom());

        var locationHistory = Mockito.mock(ProductLocationHistory.class);
        Mockito.when(locationHistory.getCreatedDate()).thenReturn(ZonedDateTime.now().minusDays(2));

        var customerTo = Mockito.mock(Customer.class);
        Mockito.when(customerTo.getCode()).thenReturn("123");

        Mockito.when(locationHistory.getCustomerTo()).thenReturn(customerTo);

        var product = Mockito.mock(Product.class);
        Mockito.when(product.getProductFamily()).thenReturn("productFamily");

        Mockito.when(locationHistory.getProduct()).thenReturn(product);

        Mockito.when(productLocationHistoryRepository.findCurrentLocation(Mockito.any())).thenReturn(Mono.just(locationHistory));

        externalTransfer.addItem(null,"unitNumber","productCode","employee-id",productLocationHistoryRepository);

        Assertions.assertEquals(1, externalTransfer.getExternalTransferItems().size());
        Assertions.assertNotNull(externalTransfer.getCustomerFrom());

        externalTransfer.addItem(null,"unitNumber2","productCode2","employee-id",productLocationHistoryRepository);
        Assertions.assertEquals(2, externalTransfer.getExternalTransferItems().size());
        Assertions.assertNotNull(externalTransfer.getCustomerFrom());

    }

    @Test
    public void shouldNotCompleteWhenNoProductsAdded(){

        var externalTransfer = new ExternalTransfer(1L, "123", null, "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);

        try {
            externalTransfer.complete("transfer-id","employee-id");
            Assertions.fail();
        }catch (DomainException e) {
            Assertions.assertEquals("External Transfer product list should have at least one product",e.getUseCaseMessageType().getMessage());
        }

    }

    @Test
    public void shouldCompleteTransfer(){

        var externalTransfer = new ExternalTransfer(1L, "456", null, "A123", LocalDate.now(), "employee-id", ExternalTransferStatus.PENDING, ZonedDateTime.now(),customerService);

        var product = Mockito.mock(Product.class);
        Mockito.when(product.getProductFamily()).thenReturn("productFamily");

        var locationHistory = Mockito.mock(ProductLocationHistory.class);
        Mockito.when(locationHistory.getCreatedDate()).thenReturn(ZonedDateTime.now().minusDays(2));

        var customerTo = Mockito.mock(Customer.class);
        Mockito.when(customerTo.getCode()).thenReturn("123");

        Mockito.when(locationHistory.getCustomerTo()).thenReturn(customerTo);

        Mockito.when(locationHistory.getProduct()).thenReturn(product);

        Mockito.when(productLocationHistoryRepository.findCurrentLocation(Mockito.any())).thenReturn(Mono.just(locationHistory));

        externalTransfer.addItem(null,"unitNumber","productCode","employee-id",productLocationHistoryRepository);

        externalTransfer.complete("transferId","employee-id");

        Assertions.assertEquals("transferId",externalTransfer.getHospitalTransferId());
        Assertions.assertEquals("COMPLETE",externalTransfer.getStatus().name());
        Assertions.assertFalse(externalTransfer.getProductLocationHistories().isEmpty());

    }

}
