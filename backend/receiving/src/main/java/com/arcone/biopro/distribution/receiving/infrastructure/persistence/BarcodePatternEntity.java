package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.enumeration.ParseType;
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

@Table(name = "lk_barcode_pattern")
@Getter
@Setter
@ToString
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BarcodePatternEntity implements Serializable {

    @Id
    @Column
    Long id;

    @NotNull
    @Column("pattern")
    String pattern;

    @NotNull
    @Column("match_groups")
    Integer matchGroups;

    @Column("parse_type")
    ParseType parseType;

}
