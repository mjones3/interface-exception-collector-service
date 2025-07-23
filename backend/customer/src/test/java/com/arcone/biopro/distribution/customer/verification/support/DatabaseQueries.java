package com.arcone.biopro.distribution.customer.verification.support;

import java.util.Arrays;
import java.util.List;

public class DatabaseQueries {

    public static String existCustomerById(int customerId) {
        return String.format("SELECT id FROM customer.bld_customer WHERE id = %s", customerId);
    }

    // We need to use prefix "TEST-" for "external_id" in order to delete all testing customer data
    public static List<String> deleteCustomersByTestPrefixQueries() {
        return Arrays.asList(
                "DELETE FROM customer.bld_customer_address WHERE customer_id IN (SELECT id FROM customer.bld_customer WHERE external_id LIKE 'TEST-%')",
                "DELETE FROM customer.bld_customer WHERE external_id LIKE 'TEST-%'"
        );
    }
}
