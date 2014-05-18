package de.hska.livingdocuments.core.persistence.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "subscription", uniqueConstraints = @UniqueConstraint(columnNames = {"subscriber_id", "ref_id"}))
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "subscriber_id")
    private User subscriber;

    @Column(name = "ref_id")
    private String refId;

    @Column(name = "type")
    private Type type;

    @Column(name ="created_at")
    private Date createdAt;

    public Subscription(String refId, Type type, User subscriber) {
        this.refId = refId;
        this.type = type;
        this.subscriber = subscriber;
        this.createdAt = new Date();
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public enum Type {
        DOCUMENT, ATTACHMENT, COMMENT, DISCUSSION, USER
    }
}