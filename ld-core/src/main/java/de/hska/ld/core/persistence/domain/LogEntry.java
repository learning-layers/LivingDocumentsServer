package de.hska.ld.core.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "ld_log_entry")
public class LogEntry {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "uuid", columnDefinition = "BINARY(16)")
    private UUID id;


    public UUID getId() {
        return id;
    }

    public void setId(UUID i) {
        id = i;
    }

    private String action;

    private Class[] references;

    private Long[] ids;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Date date;
    private String type;

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

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
