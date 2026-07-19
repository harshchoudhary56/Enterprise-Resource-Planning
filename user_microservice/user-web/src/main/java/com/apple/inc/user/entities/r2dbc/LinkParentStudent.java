package com.apple.inc.user.entities.r2dbc;

import com.apple.inc.user.constants.RelationShipType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name  = "tbl_link_parent_student")
public class LinkParentStudent extends AuditableEntity {

    @Column("parent_id")
    private Long parentId;

    @Column("student_id")
    private Long studentId;

    @Column("relationship_type")
    private RelationShipType relationShipType;
}
