package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "bld_product_location_history")
@ToString
@EqualsAndHashCode
public class ProductLocationHistoryEntity implements Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("customer_code_to")
    private String customerCodeTo;

    @NotNull
    @Column("customer_name_to")
    @Size(max = 255)
    private String customerNameTo;

    @Column("customer_code_from")
    private String customerCodeFrom;

    @Column("customer_name_from")
    @Size(max = 255)
    private String customerNameFrom;

    @NotNull
    @Column("history_type")
    private String type;

    @NotNull
    @Column("unit_number")
    private String unitNumber;

    @NotNull
    @Column("product_code")
    private String productCode;

    @Size(max = 255)
    @Column("product_family")
    private String productFamily;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Column("delete_date")
    private ZonedDateTime deleteDate;

    @Column("created_by_employee_id")
    private String createdByEmployeeId;

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
