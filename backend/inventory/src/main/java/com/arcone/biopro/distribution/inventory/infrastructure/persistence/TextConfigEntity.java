package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("lk_text_config")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TextConfigEntity {

    @Id
    @Column("id")
    UUID id;

    @Column("context")
    String context;

    @Column("key_code")
    String keyCode;

    @Column("text")
    String text;
}
