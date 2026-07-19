package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "tbl_department")
@EqualsAndHashCode(callSuper = true)
public class Department extends AuditableEntity {

    private String name;
    private String description;
}
