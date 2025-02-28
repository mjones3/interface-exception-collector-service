package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.beans.ConstructorProperties;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Table(name = "bld_external_transfer")
@ToString
@EqualsAndHashCode
public class ExternalTransferEntity implements Persistable<Long> {

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
    @Column("status")
    private String status;

    @Column("hospital_transfer_id")
    private String hospitalTransferId;

    @NotNull
    @Column("transfer_date")
    private LocalDate transferDate;

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
