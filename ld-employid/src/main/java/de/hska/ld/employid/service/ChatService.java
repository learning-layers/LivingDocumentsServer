package de.hska.ld.employid.service;

import de.hska.ld.employid.persistence.domain.ChatMessage;

public interface ChatService {

    ChatMessage sendMessage(Long meetingId, ChatMessage message);
}
