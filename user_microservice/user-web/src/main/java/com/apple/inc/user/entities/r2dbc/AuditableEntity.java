package com.apple.inc.user.entities.r2dbc; // Updated package name conceptually

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AuditableEntity extends BaseEntity {

    @CreatedDate
    @Column("created_date")
    private String createdDate;

    @LastModifiedDate
    @Column("updated_date")
    private String updatedDate;
}