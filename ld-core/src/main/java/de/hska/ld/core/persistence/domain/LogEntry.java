package de.hska.ld.core.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;

@Entity
@Table(name = "log_entry")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Long id;

    private String action;

    private Class[] references;

    private Long[] ids;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Date date;

    public Long[] getIds() {
        return ids;
    }

    public void setIds(Long[] ids) {
        this.ids = ids;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @JsonIgnore
    public Class[] getReferences() {
        return references;
    }

    public void setReferences(Class[] references) {
        this.references = references;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty
    public String[] getRefs() {
        String[] refs = new String[references.length];
        for (int idx = 0; idx < references.length; idx++) {
            refs[idx] = references[0].getSimpleName();
        }
        return refs;
    }

    public void setRefs(String[] refs) {
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "action='" + action + '\'' +
                ", references=" + Arrays.toString(references) +
                ", ids=" + Arrays.toString(ids) +
                '}';
    }
}
