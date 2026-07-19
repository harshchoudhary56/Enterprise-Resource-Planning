package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "tbl_book")
@EqualsAndHashCode(callSuper = true)
public class Book extends AuditableEntity {

    private String title;
    private String publication;
    private String isbn;
}
