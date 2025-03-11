package com.arcone.biopro.distribution.order.unit.adapter.in.web.dto;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.PageDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class PageDTOTest {

    private static final List<Integer> TEN_RECORDS_FROM_ZERO_TO_NINE = IntStream.range(0, 10).boxed().toList();
    private static final List<Integer> FIVE_RECORDS_FROM_TEN_TO_FOURTEEN = IntStream.range(10, 15).boxed().toList();

    @Test
    void testComputedValuesForSinglePage() {
        var page = new PageDTO<>(TEN_RECORDS_FROM_ZERO_TO_NINE, 0, 10, 10, null);
        assertEquals(10, page.content().size());
        assertFalse(page.hasPrevious());
        assertFalse(page.hasNext());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());
        assertEquals(1, page.totalPages()); // Sigle page of 10 items
    }

    @Test
    void testComputedValuesForFirstPageOutOf3() {
        var page = new PageDTO<>(TEN_RECORDS_FROM_ZERO_TO_NINE, 0, 10, 25, null);
        assertEquals(10, page.content().size());
        assertFalse(page.hasPrevious());
        assertTrue(page.hasNext());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
        assertEquals(3, page.totalPages()); // 2 pages of 10 records each and the 3rd page with the remaining 5 records
    }

    @ParameterizedTest
    @CsvSource({
        "1,3,10,25", // picking 2nd page (1): 3 pages of 10 records each for a total of 25 records
        "2,5,5,25",  // picking 3rd page (2): 5 pages of 5 records each for a total of 25 records
        "3,9,3,25"   // picking 4th page (3): 9 pages of 3 records each for a total of 25 records
    })
    void testComputedValuesForPagesNotFirstNorLast(int pageNumber, int totalPages, int pageSize, long totalRecords) {
        var page = new PageDTO<>(TEN_RECORDS_FROM_ZERO_TO_NINE, pageNumber, pageSize, totalRecords, null);
        assertEquals(10, page.content().size());
        assertTrue(page.hasPrevious());
        assertTrue(page.hasNext());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
        assertEquals(totalPages, page.totalPages());
    }

    @Test
    void testComputedValuesForLastPageOutOf3() {
        var page = new PageDTO<>(FIVE_RECORDS_FROM_TEN_TO_FOURTEEN, 2, 10, 25, null);
        assertEquals(5, page.content().size());
        assertTrue(page.hasPrevious());
        assertFalse(page.hasNext());
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
        assertEquals(3, page.totalPages());
    }

}
