package com.arcone.biopro.distribution.order.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "bld_order")
public class OrderEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("order_number")
    private Long orderNumber;

    @Column("external_id")
    private String externalId;

    @NotNull
    @Column("location_code")
    private String locationCode;

    @NotNull
    @Column("shipment_type")
    private String shipmentType;

    @NotNull
    @Column("shipping_method")
    private String shippingMethod;

    @NotNull
    @Column("shipping_customer_name")
    private String shippingCustomerName;

    @NotNull
    @Column("shipping_customer_code")
    private String shippingCustomerCode;

    @NotNull
    @Column("billing_customer_name")
    private String billingCustomerName;

    @NotNull
    @Column("billing_customer_code")
    private String billingCustomerCode;

    @NotNull
    @Column("desired_shipping_date")
    private LocalDate desiredShippingDate;

    @Column("will_call_pickup")
    private Boolean willCallPickup;

    @Column("phone_number")
    private String phoneNumber;

    @NotNull
    @Column("product_category")
    private String productCategory;

    @Column("comments")
    private String comments;

    @NotNull
    @Column("status")
    private String status;

    @NotNull
    @Column("priority")
    private String priority;

    @NotNull
    @Column("create_employee_id")
    private String createEmployeeId;

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

}
