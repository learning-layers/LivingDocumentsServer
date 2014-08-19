package de.hska.ld.content.persistence.domain;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ld_hyperlink")
public class Hyperlink extends Content {

    @NotBlank
    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "description")
    private String description;

}
