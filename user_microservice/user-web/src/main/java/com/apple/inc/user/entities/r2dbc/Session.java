package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table(name = "tbl_session")
@EqualsAndHashCode(callSuper = true)
public class Session extends AuditableEntity {

    private String accessToken;
    private String refreshToken;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String deviceInfo;

    @Column("user_id")
    private Long userId;
}
