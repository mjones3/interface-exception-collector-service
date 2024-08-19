package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
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
import java.time.LocalDate;
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
    @Column("order_number")
    private Long orderNumber;

    @NotNull
    @Column("customer_code")
    private Long customerCode;

    @NotNull
    @Column("customer_name")
    @Size(max = 255)
    private String customerName;

    @NotNull
    @Column("location_code")
    private String locationCode;

    @NotNull
    @Size(max = 255)
    @Column("delivery_type")
    private String deliveryType;

    @NotNull
    @Size(max = 255)
    @Column("shipment_method")
    private String shipmentMethod;

    @NotNull
    @Column("product_category")
    private String productCategory;

    @NotNull
    @Size(max = 255)
    @Column("priority")
    private ShipmentPriority priority;

    @NotNull
    @Size(max = 255)
    @Column("status")
    private ShipmentStatus status;

    @Column("customer_phone_number")
    @Size(min = 1, max = 255)
    private String customerPhoneNumber;

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

    @Size(max = 255)
    @Column("address_contact_name")
    private String addressContactName;

    @NotNull
    @Column("shipping_date")
    private LocalDate shippingDate;

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
    @Column("completed_by_employee_id")
    private String completedByEmployeeId;

    @Column("comments")
    private String comments;

    @Column("department_name")
    private String departmentName;

    @Column("complete_date")
    private ZonedDateTime completeDate;

    @Override
    public boolean isNew() {
        return createDate == null;
    }

}
