package com.arcone.biopro.distribution.shippingservice.domain.model;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder
@Table(name = "bld_order")
public class Order implements Serializable, Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("order_number")
    private Long orderNumber;

    @Column("shipping_customer_code")
    @NotNull
    private Long shippingCustomerCode;

    @Column("billing_customer_code")
    @NotNull
    private Long billingCustomerCode;

    @NotNull
    @Column("location_code")
    private Long locationCode;

    @NotNull
    @Size(max = 255)
    @Column("delivery_type")
    private String deliveryType;

    @NotNull
    @Size(max = 255)
    @Column("shipping_method")
    private String shippingMethod;

    @NotNull
    @Column("product_category")
    private String productCategory;

    @NotNull
    @Column("shipping_date")
    private LocalDate shippingDate;

    @NotNull
    @Column("priority")
    private OrderPriority priority;

    @NotNull
    @Column("status")
    @Size(max = 20)
    private OrderStatus status;

    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @NotNull
    @Column("shipping_customer_name")
    @Size(max = 255)
    private String shippingCustomerName;

    @Column("billing_customer_name")
    @Size(max = 255)
    private String billingCustomerName;

    @Column("customer_phone_number")
    @Size(min = 1, max = 255)
    private String customerPhoneNumber;

    @NotNull
    @Column("customer_address_state")
    @Size(max = 50)
    private String customerAddressState;

    @NotNull
    @Column("customer_address_postal_code")
    @Size(max = 10)
    private String customerAddressPostalCode;

    @Column("customer_address_country")
    @Size(max = 10)
    private String customerAddressCountry;

    @NotNull
    @Column("customer_address_country_code")
    @Size(max = 10)
    private String customerAddressCountryCode;

    @NotNull
    @Column("customer_address_city")
    @Size(max = 255)
    private String customerAddressCity;

    @Column("customer_address_district")
    @Size(max = 50)
    private String customerAddressDistrict;

    @NotNull
    @Column("customer_address_line1")
    @Size(max = 255)
    private String customerAddressAddressLine1;

    @Column("customer_address_line2")
    @Size(max = 255)
    private String customerAddressAddressLine2;

    @Override
    public boolean isNew() {
        return createDate == null;
    }
}
