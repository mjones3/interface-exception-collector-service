package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "lk_label_template")
public class LabelTemplateEntity {

    @Id
    @Column
    private Long id;

    @NotNull
    @Column("template_type")
    @Size(max = 200)
    private String type;

    @NotNull
    @Column("template")
    @Size(max = 1000)
    private String template;

    @NotNull
    @Column("order_number")
    private int orderNumber;

    @NotNull
    @Column("active")
    private boolean active;

    @Column("create_date")
    private ZonedDateTime createDate;

    @Column("modification_date")
    private ZonedDateTime modificationDate;


}
