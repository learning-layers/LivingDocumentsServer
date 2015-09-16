package de.hska.ld.employid.persistence.domain;

import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.employid.util.Employid;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage extends Content{

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
