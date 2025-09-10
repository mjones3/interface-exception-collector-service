package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for the new type-safe repository method to ensure parameter type
 * matching works correctly.
 */
@DataJpaTest
@ActiveProfiles("test")
public class TypeSafeRepositoryTest {

    @Autowired
    private InterfaceExceptionRepository repository;

    @Test
    void findWithFiltersTypeSafe_ShouldReturnEmptyResults_WhenNoDataExists() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        Page<InterfaceException> result = repository.findWithFiltersTypeSafe(
                InterfaceType.ORDER,
                ExceptionStatus.NEW,
                ExceptionSeverity.HIGH,
                "customer123",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now(),
                sort);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findWithFiltersTypeSafe_ShouldReturnEmptyResults_WithNullFilters() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        Page<InterfaceException> result = repository.findWithFiltersTypeSafe(
                null, null, null, null, null, null, sort);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
