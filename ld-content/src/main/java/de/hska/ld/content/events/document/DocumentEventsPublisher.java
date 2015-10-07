package de.hska.ld.content.events.document;

import de.hska.ld.content.persistence.domain.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DocumentEventsPublisher {
    @Autowired
    private ApplicationEventPublisher publisher;

    public DocumentCreationEvent sendDocumentCreationEvent(Document document) {
        DocumentCreationEvent event = new DocumentCreationEvent(document);
        this.publisher.publishEvent(event);
        return event;
    }

    public DocumentReadEvent sendDocumentReadEvent(Document document) {
        DocumentReadEvent event = new DocumentReadEvent(document);
        this.publisher.publishEvent(event);
        return event;
    }
}
