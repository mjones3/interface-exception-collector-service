package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@Table(name = "bld_shipment_item_packed")
public class ShipmentItemPacked implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("shipment_item_id")
    private Long shipmentItemId;

    @NotNull
    @Column("unit_number")
    private String unitNumber;

    @NotNull
    @Column("product_code")
    private String productCode;

    @NotNull
    @Column("product_description")
    private String productDescription;

    @Column("packed_by_employee_id")
    private String packedByEmployeeId;

    @NotNull
    @Column("abo_rh")
    private String aboRh;

    @NotNull
    @Column("expiration_date")
    private LocalDateTime expirationDate;

    @Column("collection_date")
    private ZonedDateTime collectionDate;

    @NotNull
    @Column("create_date")
    @InsertOnlyProperty
    @CreatedDate
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;
    @NotNull
    @Column("visual_inspection")
    private VisualInspection visualInspection;

    @Size(max = 255)
    @Column("product_family")
    private String productFamily;

    @Column("blood_type")
    private BloodType bloodType;

    @Column("second_verification")
    private SecondVerification secondVerification;

    @Column("verification_date")
    private ZonedDateTime verificationDate;

    @Column("verified_by_employee_id")
    private String verifiedByEmployeeId;

    @Override
    public boolean isNew() {
        return createDate == null;
    }

}
