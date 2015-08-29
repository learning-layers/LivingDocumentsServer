package de.hska.ld.content.config.message;

import de.hska.ld.core.persistence.domain.User;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class MessageAuthenticationInterceptor extends ChannelInterceptorAdapter {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            String destination = headerAccessor.getDestination();
            Object headerAccessorUser = headerAccessor.getUser();
            if (!(headerAccessorUser instanceof UsernamePasswordAuthenticationToken)) {
                throw new IllegalArgumentException("User not set");
            }
            UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) headerAccessorUser;
            User user = (User) userToken.getPrincipal();
            validateDestination(destination, user);
        }
        return super.preSend(message, channel);
    }

    private void validateDestination(String destination, User user) {
        if (destination != null && !destination.isEmpty() && destination.contains("/")) {
            String[] split = destination.split("/");
            if (split.length < 2 || !"user".equals(split[1]) && user.getUsername().equals(split[2])) {
                throwException(destination, user);
            }
        } else {
            throwException(destination, user);
        }
    }

    private void throwException(String destination, User user) {
        throw new IllegalArgumentException("User " + user.getUsername()
                + " has no permission for destination " + destination);
    }
}
