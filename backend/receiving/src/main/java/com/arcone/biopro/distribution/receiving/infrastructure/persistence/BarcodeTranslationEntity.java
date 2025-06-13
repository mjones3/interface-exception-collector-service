package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Table(name = "lk_barcode_translation")
@Getter
@Setter
@ToString
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarcodeTranslationEntity implements Serializable {

    @Id
    @Column
    Long id;

    @NotNull
    @Column("from_value")
    String fromValue;

    @NotNull
    @Column("to_value")
    String toValue;

    @Column("sixth_digit")
    String sixthDigit;
}

