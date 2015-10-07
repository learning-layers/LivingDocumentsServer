package de.hska.ld.content.events.document;

import org.springframework.context.ApplicationEvent;

public class DocumentReadEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DocumentReadEvent(Object source) {
        super(source);
    }
}
