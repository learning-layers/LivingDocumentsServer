package de.hska.ld.content.events.document;

import de.hska.ld.content.persistence.domain.Document;
import org.springframework.context.ApplicationEvent;

public class DocumentResultEvent extends ApplicationEvent {
    private static final long serialVersionUID = -1903319969171271551L;

    private Document resultDocument;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DocumentResultEvent(Object source) {
        super(source);
    }

    public Document getResultDocument() {
        return resultDocument;
    }

    public void setResultDocument(Document resultDocument) {
        this.resultDocument = resultDocument;
    }
}
