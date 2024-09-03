package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.application.dto.Product;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Comment;
import com.arcone.biopro.distribution.inventory.domain.model.vo.CreatedDate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"createDate"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductHistory implements Serializable {

    UUID id;

    UUID productId;

    ProductStatus labelStatus;

    String labelStatusReason;

    String relabelReason;

    CreatedDate createDate;

    Comment comment;

    public ProductHistory(UUID productCode, String comment){
        this.id = UUID.randomUUID();
        this.productId = productCode;
        this.comment = new Comment(comment);
    }

    public void relabel(String reason, Comment comment) {
        if(Objects.nonNull(reason) && reason.equals("OTHER")) {
            Assert.hasText(comment.value(), "Comments cannot be empty/null when the relabel reason is OTHER");
        }
        this.relabelReason = reason;
        this.comment = comment;
    }
}
