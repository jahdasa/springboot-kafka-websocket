package com.ivanfranchin.bitcoinclient.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class CustomStompInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Retrieve the accessor which provides access to the headers
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        // Ensure that you are intercepting only SUBSCRIBE frames
        if (accessor != null && SimpMessageType.SUBSCRIBE.equals(accessor.getMessageType())) {
            // Retrieve the custom headers or any standard headers
//            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
//            String topicListHeader = accessor.getFirstNativeHeader("topicList");
//            String userIdHeader = accessor.getFirstNativeHeader("userId");
            final String isinsHeader = accessor.getFirstNativeHeader("isins");

            // Example: Simple verification logic
//            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//                System.out.println("Missing or invalid Authorization header");
//                throw new IllegalArgumentException("Invalid Authorization header");
//            }
//
//            if (topicListHeader == null || topicListHeader.isEmpty()) {
//                System.out.println("topicList header is required");
//                throw new IllegalArgumentException("Missing topicList header");
//            }

//            System.out.println("Authorization: " + authorizationHeader);
//            System.out.println("Subscribed to topics: " + topicListHeader);
//            System.out.println("User ID: " + userIdHeader);
            System.out.println("isins Header: " + isinsHeader);

            // Validate further if needed (e.g., check JWT tokens, user authorization, etc.)
            // Throw an exception to terminate the subscription if headers are invalid
        }

        // Proceed with normal message flow if validation passes
        return message;
    }
}