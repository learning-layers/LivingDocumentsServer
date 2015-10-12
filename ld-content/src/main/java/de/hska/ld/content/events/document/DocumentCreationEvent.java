package de.hska.ld.content.events.document;

public class DocumentCreationEvent extends DocumentResultEvent {
    private static final long serialVersionUID = -8433782861887348259L;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DocumentCreationEvent(Object source) {
        super(source);
    }
}
