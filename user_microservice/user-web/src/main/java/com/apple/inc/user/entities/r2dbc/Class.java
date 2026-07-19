package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "tbl_class")
@EqualsAndHashCode(callSuper = true)
public class Class extends AuditableEntity {

    @Column("department_id")
    private Long departmentId;
    private String name;
    private String academicYear;
}
