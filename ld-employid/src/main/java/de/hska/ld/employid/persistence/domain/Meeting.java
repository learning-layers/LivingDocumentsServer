package de.hska.ld.employid.persistence.domain;

import de.hska.ld.employid.util.Employid;
import org.hibernate.search.annotations.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Indexed
@Table(name = "eid_meeting")
public class Meeting extends Content {

    @NotBlank
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
