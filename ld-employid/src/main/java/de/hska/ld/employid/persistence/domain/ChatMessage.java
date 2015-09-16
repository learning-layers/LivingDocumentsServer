package de.hska.ld.employid.persistence.domain;

import de.hska.ld.core.persistence.domain.User;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage extends ContentEID {

    private String message;

    public String getMessage() { return message;}
    public void setMessage(String message) { this.message = message;}

    private List<User> recipients;

    public List<User> getRecipients() {
        if (recipients == null) {
            recipients = new ArrayList<>();
        }
        return recipients;
    }
    public void setRecipients(List<User> recipients) {
        this.recipients = recipients;
    }
}
