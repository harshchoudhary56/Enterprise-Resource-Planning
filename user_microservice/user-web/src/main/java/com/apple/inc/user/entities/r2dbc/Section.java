package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "tbl_section")
@EqualsAndHashCode(callSuper = true)
public class Section extends AuditableEntity {

    private String name;

    @Column("class_id")
    private Long classId;
}
