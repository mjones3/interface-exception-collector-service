package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("bld_device")
public class DeviceEntity implements Serializable, Persistable<Long> {

    @Id
    private Long id;

    @Size(max = 50)
    @Column("type")
    private String type;

    @Size(max = 50)
    @Column("category")
    private String category;

    @Size(max = 50)
    @Column("serial_number")
    private String serialNumber;

    @Size(max = 50)
    @NotNull
    @Column("blood_center_id")
    private String bloodCenterId;

    @Size(max = 50)
    @Column("location")
    private String location;

    @Size(max = 50)
    @Column("name")
    private String name;

    @Column("active")
    private Boolean active;

    @CreatedDate
    @NotNull
    @Column("create_date")
    private ZonedDateTime createDate;

    @LastModifiedDate
    @NotNull
    @Column("modification_date")
    private ZonedDateTime modificationDate;

    @JsonIgnore
    public boolean isNew() {
        return id == null;
    }


}
