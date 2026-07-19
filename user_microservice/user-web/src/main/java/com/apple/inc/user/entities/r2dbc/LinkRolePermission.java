package com.apple.inc.user.entities.r2dbc;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "tbl_link_role_permission")
public class LinkRolePermission extends AuditableEntity {

    @Column("role_id")
    private Long roleId;

    @Column("permission_id")
    private Long permissionId;

}
