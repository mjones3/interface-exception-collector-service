package com.arcone.biopro.distribution.irradiation.infrastructure.persistence.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Table("lk_configuration")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigurationEntity implements Serializable, Persistable<String> {

    @Id
    @Column("key")
    String key;

    @Column("value")
    String value;
    
    @Column("active")
    Boolean active;
    
    @Column("create_date")
    LocalDateTime createDate;
    
    @Column("modification_date")
    LocalDateTime modificationDate;

    @Override
    public String getId() {
        return this.key;
    }

    @Override
    public boolean isNew() {
        return key == null;
    }
}

