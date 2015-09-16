package de.hska.ld.employid.controller;

import de.hska.ld.core.util.Core;
import de.hska.ld.employid.persistence.domain.ChatMessage;
import de.hska.ld.employid.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.Callable;

@Controller
public class ChatController {

    /*@Autowired
    private ChatService chatService;*/

    /*@Secured(Core.ROLE_USER)
    @MessageMapping("/meeting/{meetingId}/message")
    @SendTo("/meeting/{meetingId}/chat")
    public ChatMessage sendMessage(@PathVariable Long meetingId, @RequestBody ChatMessage message) {
        return () -> {

        };
    }*/
}
