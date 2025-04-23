package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "bld_recovered_plasma_shipment_carton_item")
public class CartonItemEntity {

    @Id
    @InsertOnlyProperty
    private Long id;

    @Column("carton_id")
    private Long cartonId;

    @Column("unit_number")
    private String unitNumber;

    @Column("product_code")
    private String productCode;

    @Column("product_type")
    private String productType;

    @Column("product_description")
    private String productDescription;

    @Column("packed_by_employee_id")
    private String packedByEmployeeId;

    @Column("abo_rh")
    private String aboRh;

    @Column("expiration_date")
    private LocalDateTime expirationDate;

    @Column("collection_date")
    private ZonedDateTime collectionDate;

    @Column("status")
    private String status;

    @Column("volume")
    private Integer volume;

    @Column("weight")
    private Integer weight;

    @Column("create_date")
    @InsertOnlyProperty
    @CreatedDate
    private ZonedDateTime createDate;

    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;


}
