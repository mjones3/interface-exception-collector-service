package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Table(name = "lk_import_fin_number")
@Getter
@Setter
@ToString
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinNumberEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("fin_number")
    private String finNumber;

    @Column("order_number")
    private Integer orderNumber;

    @Column("active")
    private boolean active;

    @Column("create_date")
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Column("modification_date")
    private ZonedDateTime modificationDate;

}
