package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.error.DataNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentCustomerTest {

    @Mock
    private CustomerService customerService;

    private CustomerOutput mockCustomerOutput;

    @BeforeEach
    void setUp() {
        mockCustomerOutput = CustomerOutput.builder()
            .name("Test Customer")
            .code("TEST001")
            .state("CA")
            .postalCode("12345")
            .country("United States")
            .countryCode("US")
            .city("Test City")
            .district("Test District")
            .addressLine1("123 Test St")
            .addressLine2("Suite 100")
            .build();
    }

    @Nested
    @DisplayName("fromCustomerCode tests")
    class FromCustomerCodeTests {

        @Test
        @DisplayName("Should create ShipmentCustomer successfully from customer code")
        void shouldCreateShipmentCustomerFromCode() {
            // Arrange
            when(customerService.findByCode("TEST001")).thenReturn(Mono.just(mockCustomerOutput));

            // Act
            ShipmentCustomer result = ShipmentCustomer.fromCustomerCode("TEST001", customerService);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getCustomerCode()).isEqualTo("TEST001");
            assertThat(result.getCustomerName()).isEqualTo("Test Customer");
            assertThat(result.getCustomerState()).isEqualTo("CA");
            assertThat(result.getCustomerPostalCode()).isEqualTo("12345");
            assertThat(result.getCustomerCountry()).isEqualTo("United States");
            assertThat(result.getCustomerCountryCode()).isEqualTo("US");
            assertThat(result.getCustomerCity()).isEqualTo("Test City");
            assertThat(result.getCustomerDistrict()).isEqualTo("Test District");
            assertThat(result.getCustomerAddressLine1()).isEqualTo("123 Test St");
            assertThat(result.getCustomerAddressLine2()).isEqualTo("Suite 100");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Arrange
            when(customerService.findByCode("INVALID")).thenReturn(Mono.empty());

            // Act & Assert
            assertThatThrownBy(() -> ShipmentCustomer.fromCustomerCode("INVALID", customerService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found for code: INVALID");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when DataNotFoundException occurs")
        void shouldThrowExceptionWhenDataNotFoundExceptionOccurs() {
            // Arrange
            when(customerService.findByCode("TEST001")).thenReturn(Mono.empty());

            // Act & Assert
            assertThatThrownBy(() -> ShipmentCustomer.fromCustomerCode("TEST001", customerService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found for code: TEST001");
        }
    }

    @Nested
    @DisplayName("fromShipmentDetails tests")
    class FromShipmentDetailsTests {

        @Test
        @DisplayName("Should create ShipmentCustomer successfully from shipment details")
        void shouldCreateShipmentCustomerFromDetails() {
            // Act
            ShipmentCustomer result = ShipmentCustomer.fromShipmentDetails(
                "TEST001",
                "Test Customer",
                "CA",
                "12345",
                "United States",
                "US",
                "Test City",
                "Test District",
                "123 Test St",
                "Suite 100",
                "John Doe",
                "555-1234",
                "Sales"
            );

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getCustomerCode()).isEqualTo("TEST001");
            assertThat(result.getCustomerName()).isEqualTo("Test Customer");
            assertThat(result.getCustomerState()).isEqualTo("CA");
            assertThat(result.getCustomerPostalCode()).isEqualTo("12345");
            assertThat(result.getCustomerCountry()).isEqualTo("United States");
            assertThat(result.getCustomerCountryCode()).isEqualTo("US");
            assertThat(result.getCustomerCity()).isEqualTo("Test City");
            assertThat(result.getCustomerDistrict()).isEqualTo("Test District");
            assertThat(result.getCustomerAddressLine1()).isEqualTo("123 Test St");
            assertThat(result.getCustomerAddressLine2()).isEqualTo("Suite 100");
            assertThat(result.getCustomerAddressContactName()).isEqualTo("John Doe");
            assertThat(result.getCustomerAddressPhoneNumber()).isEqualTo("555-1234");
            assertThat(result.getCustomerAddressDepartmentName()).isEqualTo("Sales");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer code is null")
        void shouldThrowExceptionWhenCustomerCodeIsNull() {
            assertThatThrownBy(() -> ShipmentCustomer.fromShipmentDetails(
                null,
                "Test Customer",
                "CA",
                "12345",
                "United States",
                "US",
                "Test City",
                "Test District",
                "123 Test St",
                "Suite 100",
                "John Doe",
                "555-1234",
                "Sales"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer code cannot be null or blank");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer code is blank")
        void shouldThrowExceptionWhenCustomerCodeIsBlank() {
            assertThatThrownBy(() -> ShipmentCustomer.fromShipmentDetails(
                "",
                "Test Customer",
                "CA",
                "12345",
                "United States",
                "US",
                "Test City",
                "Test District",
                "123 Test St",
                "Suite 100",
                "John Doe",
                "555-1234",
                "Sales"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer code cannot be null or blank");
        }
    }
}

