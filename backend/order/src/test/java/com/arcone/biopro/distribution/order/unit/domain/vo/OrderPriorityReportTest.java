package com.arcone.biopro.distribution.order.unit.domain.vo;

import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriorityReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderPriorityReportTest {

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new OrderPriorityReport(null, null));
        assertThrows(IllegalArgumentException.class, () -> new OrderPriorityReport("PRIORITY", null));
        assertDoesNotThrow(() -> new OrderPriorityReport("PRIORITY", "PRIORITY COLOR"));
    }

}
