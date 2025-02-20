package com.arcone.biopro.distribution.shipping.unit.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransferItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExternalTransferItemTest {

    @Test
    void shouldCreate() {

        var response = new ExternalTransferItem(1L, 1L, "unitNumber", "productCode", "employee-id");
        Assertions.assertNotNull(response);
    }

    @Test
    void shouldNotCreate() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransferItem(1L, null, "unitNumber", "productCode", "employee-id"), "External Transfer ID cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransferItem(1L, 1L, null, "productCode", "employee-id"), "Unit Number cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransferItem(1L, 1L, "unitNumber", null, "employee-id"), "Product Code cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalTransferItem(1L, 1L, "unitNumber", "productCode", null), "Employee ID cannot be null");

    }

}
