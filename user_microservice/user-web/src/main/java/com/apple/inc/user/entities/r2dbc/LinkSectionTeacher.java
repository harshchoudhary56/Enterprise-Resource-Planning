package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name ="tbl_link_section_teacher")
public class LinkSectionTeacher extends AuditableEntity {

    @Column("teacher_id")
    private Long teacherId;

    @Column("section_id")
    private Long sectionId;

    private Boolean isClassTeacher;
}
