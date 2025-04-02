package com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.graphql;

public class GraphQLQueryMapper {
    public static String findCustomerByCode(String code) {
        return (String.format("""
            query {
                findCustomerByCode(code: "%s") {
                    externalId
                    name
                    code
                    departmentCode
                    departmentName
                    phoneNumber
                    active
                    addresses {
                        contactName
                        addressType
                        state
                        postalCode
                        countryCode
                        city
                        district
                        addressLine1
                        addressLine2
                        active
                    }
                }
            }
            """, code));
    }

    public static String findAllLocations() {
        return (String.format("""
            query {
                findAllLocations {
                    id
                    name
                    code
                    externalId
                    addressLine1
                    addressLine2
                    postalCode
                    city
                    state
                    properties
                }
            }
            """));
    }

    public static String findAllCustomers() {
        return (String.format("""
            query {
                findAllCustomers {
                    externalId
                    customerType
                    name
                    code
                    departmentCode
                    departmentName
                    foreignFlag
                    phoneNumber
                    contactName
                    state
                    postalCode
                    country
                    countryCode
                    city
                    district
                    addressLine1
                    addressLine2
                    active
                    createDate
                    modificationDate
                }
            }
            """));
    }

    public static String findAllProductTypeCustomer(String customerCode) {
        return String.format("""
            query {
                findAllProductTypeByCustomer(customerCode:"%s") {
                    id
                   productType
                   productTypeDescription
                }
            }
            """, customerCode);
    }
}
