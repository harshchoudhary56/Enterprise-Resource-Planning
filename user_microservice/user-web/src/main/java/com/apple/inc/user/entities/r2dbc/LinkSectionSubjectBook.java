package com.apple.inc.user.entities.r2dbc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "tbl_link_section_subject_book")
public class LinkSectionSubjectBook extends AuditableEntity {

    @Column("section_id")
    private Long sectionId;

    @Column("subject_id")
    private Long subjectId;

    @Column("book_id")
    private Long book_id;
}
