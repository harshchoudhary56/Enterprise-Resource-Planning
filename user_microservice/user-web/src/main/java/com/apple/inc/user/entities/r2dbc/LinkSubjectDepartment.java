package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "tbl_link_subject_department")
public class LinkSubjectDepartment extends AuditableEntity {

    @Column("subject_id")
    private Long subjectId;

    @Column("department_id")
    private Long departmentId;
}
