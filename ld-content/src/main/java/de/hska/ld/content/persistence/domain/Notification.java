package de.hska.ld.content.persistence.domain;

import javax.persistence.*;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "subscriber_id")
    private Long subscriberId;

    @Column(name = "editor_id")
    private Long editorId;

    @Column(name = "type")
    private Subscription.Type type;

    @Column(name = "markedAsRead")
    private boolean markedAsRead;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }

    public Long getEditorId() {
        return editorId;
    }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
    }

    public Subscription.Type getType() {
        return type;
    }

    public void setType(Subscription.Type type) {
        this.type = type;
    }

    public boolean isMarkedAsRead() {
        return markedAsRead;
    }

    public void setMarkedAsRead(boolean markedAsRead) {
        this.markedAsRead = markedAsRead;
    }
}
