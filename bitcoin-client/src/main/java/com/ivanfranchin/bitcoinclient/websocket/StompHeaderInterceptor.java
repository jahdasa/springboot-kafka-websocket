package com.ivanfranchin.bitcoinclient.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompHeaderInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Get mutable header accessor
        MessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, MessageHeaderAccessor.class);
        if (accessor != null) {
            // Remove specific headers
//            accessor.removeHeader("subscription");
            accessor.removeHeader("contentType");
        }
        // Return the modified message
        return message;
    }
}