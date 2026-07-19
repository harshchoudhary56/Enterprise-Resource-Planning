package com.apple.inc.user.entities.r2dbc;

import com.apple.inc.user.constants.Gender;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "tbl_user")
@EqualsAndHashCode(callSuper = true)
public class User extends AuditableEntity {

    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private Gender gender;

    @Column("section_id")
    private Section section;

    private String rollNo;
    private String employeeId;
    private String qualification;
}
