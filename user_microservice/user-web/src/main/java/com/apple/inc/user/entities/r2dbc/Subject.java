package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "tbl_subject")
public class Subject extends AuditableEntity {

    private String name;
    private String code;
    private String description;
}
