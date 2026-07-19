package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "tbl_address")
@EqualsAndHashCode(callSuper = true)
public class Address extends AuditableEntity {

    @Column("user_id")
    private Long userId;

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
