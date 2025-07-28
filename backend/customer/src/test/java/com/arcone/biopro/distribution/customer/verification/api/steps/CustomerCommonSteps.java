package com.arcone.biopro.distribution.customer.verification.api.steps;

import com.arcone.biopro.distribution.customer.domain.model.Customer;
import com.arcone.biopro.distribution.customer.domain.model.CustomerAddress;
import com.arcone.biopro.distribution.customer.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.customer.domain.repository.CustomerAddressRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerCommonSteps {

    private final Map<String, Customer> createdCustomers = new HashMap<>();
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    public CustomerCommonSteps(CustomerRepository customerRepository, CustomerAddressRepository customerAddressRepository) {
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
    }

    private String cleanEmpty(String value) {
        return value != null ? value.replace("<EMPTY>", "") : null;
    }

    private Mono<Customer> createCustomerFromData(Map<String, String> customerData) {
        // Build the customer object with all available fields
        Customer customer = new Customer();
        customer.setExternalId(customerData.get("External ID"));
        customer.setName(customerData.get("Name"));
        customer.setCode(customerData.get("Code"));
        customer.setDepartmentCode(customerData.get("Department Code"));
        customer.setDepartmentName(customerData.get("Department Name"));
        customer.setPhoneNumber(customerData.get("Phone Number"));
        customer.setForeignFlag(customerData.get("Foreign Flag"));
        customer.setCustomerType(customerData.get("Customer Type"));
        customer.setActive(customerData.get("Status"));

        return customerRepository.save(customer)
            .flatMap(savedCustomer -> {
                // Create address if address data is provided
                if (customerData.get("Address Line 1") != null ||
                    customerData.get("City") != null ||
                    customerData.get("State") != null) {

                    CustomerAddress address = new CustomerAddress();
                    address.setCustomerId(savedCustomer.getId());
                    address.setContactName(cleanEmpty(customerData.get("Contact Name")));
                    address.setAddressType(cleanEmpty(customerData.get("Address Type")));
                    address.setAddressLine1(cleanEmpty(customerData.get("Address Line 1")));
                    address.setAddressLine2(cleanEmpty(customerData.get("Address Line 2")));
                    address.setCity(cleanEmpty(customerData.get("City")));
                    address.setState(cleanEmpty(customerData.get("State")));
                    address.setPostalCode(cleanEmpty(customerData.get("Postal Code")));
                    address.setDistrict(cleanEmpty(customerData.get("District")));
                    address.setCountry(cleanEmpty(customerData.get("Country")));
                    address.setActive("Y");
                    address.setCreateDate(ZonedDateTime.now());
                    address.setModificationDate(ZonedDateTime.now());

                    return customerAddressRepository.save(address)
                        .thenReturn(savedCustomer);
                }
                return Mono.just(savedCustomer);
            });
    }

    @Given("I have the following test customers existing in the system")
    public void iHaveTheFollowingTestDataExistingInTheSystem(DataTable dataTable) {
        List<Map<String, String>> customerData = dataTable.asMaps();
        Flux.fromIterable(customerData)
            .flatMap(this::createCustomerFromData)
            .doOnNext(createdCustomer -> createdCustomers.put(createdCustomer.getExternalId(), createdCustomer))
            .blockLast(); // Block until all customers are created
    }
}
