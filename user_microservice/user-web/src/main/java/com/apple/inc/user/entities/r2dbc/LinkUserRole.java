package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "tbl_link_user_role")
@EqualsAndHashCode(callSuper = true)
public class LinkUserRole extends AuditableEntity {

    @Column("user_id")
    private Long user_id;

    @Column("role_id")
    private Long role_id;
}
