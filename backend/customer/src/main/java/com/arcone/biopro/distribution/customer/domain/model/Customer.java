package com.arcone.biopro.distribution.customer.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("customer.bld_customer")
public class Customer {
    @Id
    private Long id;

    @Column("external_id")
    private String externalId;

    private String name;
    private String code;

    @Column("department_code")
    private String departmentCode;

    @Column("department_name")
    private String departmentName;

    @Column("phone_number")
    private String phoneNumber;

    @Column("foreign_flag")
    private String foreignFlag;

    @Column("customer_type")
    private String customerType;

    private String active;
}
