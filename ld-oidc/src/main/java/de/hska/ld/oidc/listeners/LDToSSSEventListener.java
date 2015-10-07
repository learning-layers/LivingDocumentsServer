package de.hska.ld.oidc.listeners;

import de.hska.ld.content.events.document.DocumentCreationEvent;
import de.hska.ld.content.events.document.DocumentReadEvent;
import de.hska.ld.content.persistence.domain.Document;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LDToSSSEventListener {

    @EventListener
    public void handleDocumentReadEvent(DocumentReadEvent event) {
        Document newDocument = (Document) event.getSource();
        System.out.println("LDToSSSEventListener: Reading document=" + newDocument.getId() + ", title=" + newDocument.getTitle());
    }

    @EventListener
    public void handleDocumentCreationEvent(DocumentCreationEvent event) {
        Document newDocument = (Document) event.getSource();
        System.out.println("LDToSSSEventListener: Creating document=" + newDocument.getId() + ", title=" + newDocument.getTitle());
    }
}