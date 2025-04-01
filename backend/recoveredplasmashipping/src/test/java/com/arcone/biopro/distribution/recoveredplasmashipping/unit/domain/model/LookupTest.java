package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LookupEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Lookup.fromRepository;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LookupTest {

    @Test
    void shouldCreateFromRepository() {
        assertDoesNotThrow(() -> fromRepository(
            LookupEntity.builder()
                .id(1L)
                .type("type")
                .optionValue("optionValue")
                .descriptionKey("descriptionKey")
                .build())
        );
    }

    @ParameterizedTest
    @CsvSource({
        ",,,",
        "1,,,",
        "1,type,,",
        "1,type,optionValue,",
    })
    void shouldNotCreateFromRepository(
        Long id,
        String type,
        String optionValue,
        String descriptionKey
    ) {
        assertThrows(IllegalArgumentException.class, () -> fromRepository(
            LookupEntity.builder()
                .id(id)
                .type(type)
                .optionValue(optionValue)
                .descriptionKey(descriptionKey)
                .build())
        );
    }

}
