package com.apple.inc.user.entities.r2dbc;

import org.springframework.data.annotation.Id;
import lombok.Data;

@Data
public abstract class BaseEntity {

    @Id
    private Long id;
}
