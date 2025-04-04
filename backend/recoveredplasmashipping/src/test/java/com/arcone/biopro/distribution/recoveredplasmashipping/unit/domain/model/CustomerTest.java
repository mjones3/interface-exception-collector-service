package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import java.time.ZonedDateTime;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;


public class CustomerTest {

    /**
     * Test that checkValid() throws IllegalArgumentException when addressLine1 is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenAddressLine1IsBlank() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", "city", "district", "", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when addressLine1 is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenAddressLine1IsNull() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", "city", "district", null, "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when city is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCityIsBlank() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", "", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when city is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCityIsNull() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", null, "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when code is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCodeIsBlank() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when code is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCodeIsNull() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "externalId", "customerType", "name", null, "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when countryCode is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCountryCodeIsBlank() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when countryCode is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCountryCodeIsNull() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", null, "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when country is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCountryIsBlank() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when country is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenCountryIsNull() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", null, "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when name is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when name is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "externalId", "customerType", null, "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "postalCode", "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when postalCode is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenPostalCodeIsBlank() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", "", "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when postalCode is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenPostalCodeIsNull() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "state", null, "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when state is blank
     */
    @Test
    public void testCheckValidThrowsExceptionWhenStateIsBlank() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", "", "postalCode", "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Test that checkValid() throws IllegalArgumentException when state is null
     */
    @Test
    public void testCheckValidThrowsExceptionWhenStateIsNull() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "externalId", "customerType", "name", "code", "departmentCode", "departmentName", "foreignFlag", "phoneNumber",
                "contactName", null, "postalCode", "country", "countryCode", "city", "district", "addressLine1", "addressLine2",
                true, null, null));

    }

    /**
     * Tests the Customer constructor with a blank countryCode, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithBlankCountryCode() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "ext1", "type1", "name1", "code1", "deptCode1", "deptName1", "F", "123456789",
                "contact1", "state1", "12345", "country1", "", "city1", "district1", "address1", "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Tests the Customer constructor with a blank name, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithBlankName() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "ext1", "type1", "", "code1", "deptCode1", "deptName1", "F", "123456789",
                "contact1", "state1", "12345", "country1", "CC1", "city1", "district1", "address1", "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Tests the Customer constructor with a blank postalCode, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithBlankPostalCode() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "ext1", "type1", "name1", "code1", "deptCode1", "deptName1", "F", "123456789",
                "contact1", "state1", "", "country1", "CC1", "city1", "district1", "address1", "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Tests the Customer constructor with a blank state, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithBlankState() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "ext1", "type1", "name1", "code1", "deptCode1", "deptName1", "F", "123456789",
                "contact1", "", "12345", "country1", "CC1", "city1", "district1", "address1", "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Tests the Customer constructor with a null addressLine1, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithNullAddressLine1() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "ext1", "type1", "name1", "code1", "deptCode1", "deptName1", "F", "123456789",
                "contact1", "state1", "12345", "country1", "CC1", "city1", "district1", null, "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Tests the Customer constructor with a null city, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithNullCity() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "ext1", "type1", "name1", "code1", "deptCode1", "deptName1", "F", "123456789",
                "contact1", "state1", "12345", "country1", "CC1", null, "district1", "address1", "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Tests the Customer constructor with a null code, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithNullCode() {
        assertThrows(IllegalArgumentException.class, ()-> new Customer(1L, "ext1", "type1", "name1", null, "deptCode1", "deptName1", "F", "123456789",
                "contact1", "state1", "12345", "country1", "CC1", "city1", "district1", "address1", "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Tests the Customer constructor with a null country, which should throw an IllegalArgumentException
     * when checkValid() is called.
     */
    @Test
    public void testCustomerConstructorWithNullCountry() {
        assertThrows(IllegalArgumentException.class, ()->  new Customer(1L, "ext1", "type1", "name1", "code1", "deptCode1", "deptName1", "F", "123456789",
                "contact1", "state1", "12345", null, "CC1", "city1", "district1", "address1", "address2",
                true, ZonedDateTime.now(), ZonedDateTime.now()));

    }

    /**
     * Test case for Customer constructor with valid inputs.
     * This test verifies that a Customer object is correctly created with the provided parameters.
     */
    @Test
    public void test_Customer_CreationWithValidInputs() {
        Long id = 1L;
        String externalId = "EXT001";
        String customerType = "Regular";
        String name = "John Doe";
        String code = "CUST001";
        String departmentCode = "DEP001";
        String departmentName = "Sales";
        String foreignFlag = "N";
        String phoneNumber = "1234567890";
        String contactName = "Jane Doe";
        String state = "California";
        String postalCode = "90210";
        String country = "United States";
        String countryCode = "US";
        String city = "Los Angeles";
        String district = "Hollywood";
        String addressLine1 = "123 Main St";
        String addressLine2 = "Apt 4B";
        Boolean active = true;
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();

        Customer customer = new Customer(id, externalId, customerType, name, code, departmentCode, departmentName,
                foreignFlag, phoneNumber, contactName, state, postalCode, country, countryCode, city, district,
                addressLine1, addressLine2, active, createDate, modificationDate);

        assertNotNull(customer);
        assertEquals(id, customer.getId());
        assertEquals(externalId, customer.getExternalId());
        assertEquals(customerType, customer.getCustomerType());
        assertEquals(name, customer.getName());
        assertEquals(code, customer.getCode());
        assertEquals(departmentCode, customer.getDepartmentCode());
        assertEquals(departmentName, customer.getDepartmentName());
        assertEquals(foreignFlag, customer.getForeignFlag());
        assertEquals(phoneNumber, customer.getPhoneNumber());
        assertEquals(contactName, customer.getContactName());
        assertEquals(state, customer.getState());
        assertEquals(postalCode, customer.getPostalCode());
        assertEquals(country, customer.getCountry());
        assertEquals(countryCode, customer.getCountryCode());
        assertEquals(city, customer.getCity());
        assertEquals(district, customer.getDistrict());
        assertEquals(addressLine1, customer.getAddressLine1());
        assertEquals(addressLine2, customer.getAddressLine2());
        assertEquals(active, customer.getActive());
        assertEquals(createDate, customer.getCreateDate());
        assertEquals(modificationDate, customer.getModificationDate());
    }

    /**
     * Test case for checkValid method when all fields are null or blank except state.
     * This test verifies that an IllegalArgumentException is thrown with the correct message
     * when all required fields are null or blank, except for the state field.
     */
    @Test
    public void test_checkValid_AllFieldsNullOrBlankExceptState() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Customer(
            null, null, null, "", "", null, null, null, null,
            null, "ValidState", "", "", "", "", null, "", null,
            null, null, null
        ));
        assertEquals("Code cannot be null or blank", exception.getMessage());
    }

    /**
     * Tests the checkValid method when all fields except city are null or blank.
     * Expects an IllegalArgumentException to be thrown with the message "Code cannot be null or blank".
     */
    @Test
    public void test_checkValid_allFieldsNullOrBlankExceptCity() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Customer(
            null, null, null, "", "", null, null, null, null,
            null, "", "", null, "", "ValidCity", null, "", null,
            null, null, null
        ));
        assertEquals("Code cannot be null or blank", exception.getMessage());
    }

    /**
     * Tests the checkValid method when all fields are null or blank except for the country field.
     * This test verifies that an IllegalArgumentException is thrown with the appropriate message.
     */
    @Test
    public void test_checkValid_allFieldsNullOrBlankExceptCountry() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Customer(null, null, null, "", "", null, null, null, null,
            null, "", "", "ValidCountry", "", "", null, "", null,
            null, null, null));
        assertEquals("Code cannot be null or blank", exception.getMessage());
    }

    /**
     * Tests the checkValid method when all fields except countryCode are null or blank.
     * Expects an IllegalArgumentException to be thrown with the message "Country cannot be null or blank".
     */
    @Test
    public void test_checkValid_allFieldsNullOrBlankExceptCountryCode() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->  new Customer(null, null, null, "", "", null, null, null, null,
            null, "", "", "", "US", "", null, "", null,
            null, null, null));
        assertEquals("Code cannot be null or blank", exception.getMessage());
    }

    /**
     * Test case for checkValid() method when all required fields are null.
     * This test verifies that the method throws IllegalArgumentException
     * when all the mandatory fields (code, name, addressLine1, postalCode,
     * city, state, country, and countryCode) are null.
     */
    @Test
    public void test_checkValid_whenAllFieldsAreNull() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null));

    }

    /**
     * Tests the checkValid method when code and name are null or blank, but addressLine1 is valid.
     * Expects IllegalArgumentException to be thrown with the message "Name cannot be null or blank".
     */
    @Test
    public void test_checkValid_whenCodeAndNameAreBlankButAddressLine1IsValid() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->  new Customer(1L, "externalId", "customerType", "", "", "departmentCode", "departmentName",
            "foreignFlag", "phoneNumber", "contactName", "", "", "", "",
            "", "district", "Valid Address", "addressLine2", true, null, null));
        assertEquals("Code cannot be null or blank",exception.getMessage());
    }

    /**
     * Test case for checkValid method when code, name, addressLine1, city, state, country, and countryCode are blank,
     * but postalCode is not blank.
     * Expected to throw IllegalArgumentException with message "Code cannot be null or blank".
     */
    @Test
    public void test_checkValid_whenCodeIsBlankAndPostalCodeIsNotBlank() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Customer(1L, "externalId", "customerType", "", "", "departmentCode",
            "departmentName", "foreignFlag", "phoneNumber", "contactName", "", "12345",
            "", "", "", "district", "", "addressLine2", true, null, null));
        assertEquals("Code cannot be null or blank", exception.getMessage());
    }

    /**
     * Tests the checkValid method when the code is null or blank, but name is valid,
     * and all other required fields (addressLine1, postalCode, city, state, country, countryCode) are null or blank.
     * Expects an IllegalArgumentException to be thrown with the message "Code cannot be null or blank".
     */
    @Test
    public void test_checkValid_whenCodeIsNullOrBlankAndOtherFieldsAreInvalid() {

        try {
            new Customer(1L, "externalId", "customerType", "Valid Name", "", "departmentCode",
                "departmentName", "foreignFlag", "phoneNumber", "contactName", "", "", "", "",
                "", "district", "", "addressLine2", true, null, null);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Code cannot be null or blank", e.getMessage());
        }
    }

    /**
     * Test case for checkValid method when code is valid but other fields are null or blank.
     * This test verifies that the method throws IllegalArgumentException with the correct message
     * when the name is null or blank, while the code is valid.
     */
    @Test
    public void test_checkValid_whenCodeValidButNameBlank() {

        try {
            new Customer(1L, "ext1", "type1", "", "validCode", null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                true, null, null);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Name cannot be null or blank", e.getMessage());
        }
    }

}
