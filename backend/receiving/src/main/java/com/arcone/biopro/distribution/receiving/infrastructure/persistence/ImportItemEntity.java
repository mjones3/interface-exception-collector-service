package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table( "bld_import_item")
public class ImportItemEntity implements Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("import_id")
    private Long importId;

    @Column("unit_number")
    private String unitNumber;

    @Column("product_code")
    private String productCode;

    @Column("blood_type")
    private String bloodType;

    @Column("expiration_date")
    private LocalDateTime expirationDate;

    @Column("product_family")
    private String productFamily;

    @Column("short_description")
    private String productDescription;

    @Column( "create_employee_id")
    private String createEmployeeId;

    @CreatedDate
    @InsertOnlyProperty
    @Column( "create_date")
    private ZonedDateTime createDate;

    @LastModifiedDate
    @Column( "modification_date")
    private ZonedDateTime modificationDate;

    @Column( "delete_date")
    private ZonedDateTime deleteDate;

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
