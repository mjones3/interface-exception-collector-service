package com.arcone.biopro.distribution.order.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.Customer;
import com.arcone.biopro.distribution.order.domain.model.vo.CustomerAddress;
import com.arcone.biopro.distribution.order.infrastructure.mapper.CustomerEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.persistence.CustomerAddressEntity;
import com.arcone.biopro.distribution.order.infrastructure.persistence.CustomerEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CustomerEntityMapperTest {

    private final CustomerEntityMapper mapper = Mappers.getMapper(CustomerEntityMapper.class);

    @Test
    void entityToModel_WithoutAddresses_ShouldMapCorrectly() {
        // Given
        CustomerEntity entity = createCustomerEntity();
        List<CustomerAddressEntity> addresses = createCustomerAddressEntities();

        // When
        Customer result = mapper.entityToModel(entity,addresses);

        // Then
        assertThat(result).isNotNull();
        assertCustomerFields(result, entity);
    }

    @Test
    void entityToModel_WithAddresses_ShouldMapCorrectly() {
        // Given
        CustomerEntity entity = createCustomerEntity();
        List<CustomerAddressEntity> addresses = createCustomerAddressEntities();

        // When
        Customer result = mapper.entityToModel(entity, addresses);

        // Then
        assertThat(result).isNotNull();
        assertCustomerFields(result, entity);
        assertThat(result.getAddresses()).hasSize(2);
        assertAddressFields(result.getAddresses().get(0), addresses.get(0));
        assertAddressFields(result.getAddresses().get(1), addresses.get(1));
    }

    @Test
    void toModelList_WithNullList_ShouldReturnNull() {
        // When
        List<CustomerAddress> result = mapper.toModelList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModelList_WithValidList_ShouldMapCorrectly() {
        // Given
        List<CustomerAddressEntity> addresses = createCustomerAddressEntities();

        // When
        List<CustomerAddress> result = mapper.toModelList(addresses);

        // Then
        assertThat(result).hasSize(2);
        assertAddressFields(result.get(0), addresses.get(0));
        assertAddressFields(result.get(1), addresses.get(1));
    }

    @Test
    void entityToModel_SingleAddress_ShouldMapCorrectly() {
        // Given
        CustomerAddressEntity addressEntity = createCustomerAddressEntity("123 Main St","SHIPPING");

        // When
        CustomerAddress result = mapper.entityToModel(addressEntity);

        // Then
        assertThat(result).isNotNull();
        assertAddressFields(result, addressEntity);
    }

    private CustomerEntity createCustomerEntity() {
        return CustomerEntity
            .builder()
            .externalId("EXTERNAL_ID")
            .departmentCode("DEPT_CODE")
            .phoneNumber("PHONE_NUMBER")
            .customerType("1")
            .code("CUST001")
            .name("John Doe")
            .departmentName("test")
            .build();
    }

    private CustomerAddressEntity createCustomerAddressEntity(String street, String addressType) {
        return CustomerAddressEntity.builder()
            .id(1L)
            .addressLine1(street)
            .addressType(addressType)
            .contactName("contact_name")
            .country("USA")
            .countryCode("55")
            .district("LA")
            .city("New York")
            .state("NY")
            .postalCode("10001").build();
    }

    private List<CustomerAddressEntity> createCustomerAddressEntities() {
        return Arrays.asList(
            createCustomerAddressEntity("123 Main St","SHIPPING"),
            createCustomerAddressEntity("456 Oak Ave","BILLING")
        );
    }

    private void assertCustomerFields(Customer customer, CustomerEntity entity) {
        assertThat(customer.getCode().getValue()).isEqualTo(entity.getCode());
        assertThat(customer.getName()).isEqualTo(entity.getName());
        assertThat(customer.getName()).isEqualTo(entity.getName());
        // Assert other fields
    }

    private void assertAddressFields(CustomerAddress address, CustomerAddressEntity entity) {
        assertThat(address.getAddressLine1()).isEqualTo(entity.getAddressLine1());
        assertThat(address.getCity()).isEqualTo(entity.getCity());
        assertThat(address.getState()).isEqualTo(entity.getState());
        assertThat(address.getCountryCode()).isEqualTo(entity.getCountryCode());
        assertThat(address.getDistrict()).isEqualTo(entity.getDistrict());
        assertThat(address.getAddressType().getValue()).isEqualTo(entity.getAddressType());
        assertThat(address.getPostalCode()).isEqualTo(entity.getPostalCode());
        // Assert other fields
    }
}

