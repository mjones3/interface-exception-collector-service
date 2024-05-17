package com.arcone.biopro.distribution.shippingservice.domain.model;

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
import java.time.ZonedDateTime;

@Data
@Builder
@Table(name = "bld_shipment")
public class Shipment implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("order_id")
    private Long orderId;

    @Column("customer_code")
    private Long customerCode;

    @NotNull
    @Column("location_code")
    private Integer locationCode;

    @NotNull
    @Size(max = 255)
    @Column("delivery_type")
    private String deliveryType;

    @NotNull
    @Size(max = 255)
    @Column("shipment_method")
    private String shipmentMethod;

    @NotNull
    @Size(max = 255)
    @Column("status_key")
    private String statusKey;

    @NotNull
    @Size(max = 50)
    @Column("state")
    private String state;

    @NotNull
    @Size(max = 10)
    @Column("postal_code")
    private String postalCode;

    @NotNull
    @Size(max = 10)
    @Column("country")
    private String country;

    @NotNull
    @Size(max = 10)
    @Column("country_code")
    private String countryCode;

    @NotNull
    @Size(max = 255)
    @Column("city")
    private String city;

    @Size(max = 50)
    @Column("district")
    private String district;

    @NotNull
    @Size(max = 255)
    @Column("address_line1")
    private String addressLine1;

    @Size(max = 255)
    @Column("address_line2")
    private String addressLine2;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Size(max = 50)
    @Column("create_date_timezone")
    private String createDateTimezone;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Size(max = 50)
    @Column("modification_date_timezone")
    private String modificationDateTimezone;

    @Column("delete_date")
    private ZonedDateTime deleteDate;

    @Size(max = 50)
    @Column("delete_date_timezone")
    private String deleteDateTimezone;

    @Override
    public boolean isNew() {
        return createDate == null;
    }

}
