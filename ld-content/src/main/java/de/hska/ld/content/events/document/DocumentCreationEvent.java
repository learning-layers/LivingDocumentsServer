package de.hska.ld.content.events.document;

import org.springframework.context.ApplicationEvent;

public class DocumentCreationEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DocumentCreationEvent(Object source) {
        super(source);
    }
}
