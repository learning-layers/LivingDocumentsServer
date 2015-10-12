package de.hska.ld.content.events.document;

public class DocumentReadEvent extends DocumentResultEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DocumentReadEvent(Object source) {
        super(source);
    }
}
