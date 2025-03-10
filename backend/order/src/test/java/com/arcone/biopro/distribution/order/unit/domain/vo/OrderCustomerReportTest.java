package com.arcone.biopro.distribution.order.unit.domain.vo;

import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomerReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderCustomerReportTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new OrderCustomerReport(null, null));
        assertThrows(IllegalArgumentException.class, () -> new OrderCustomerReport("CODE", null));
        assertDoesNotThrow(() -> new OrderCustomerReport("CODE", "NAME"));
    }

}
