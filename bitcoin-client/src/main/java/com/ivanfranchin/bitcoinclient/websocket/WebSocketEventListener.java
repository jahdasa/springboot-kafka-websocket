package com.ivanfranchin.bitcoinclient.websocket;

import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private ItemSelectorService itemSelectorService;


    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    //method called when user open page in browser
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }
    
    //method called when user close page in browser
    @Async
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        final String sessionId = (String) headerAccessor.getHeader("simpSessionId");
        if(sessionId != null) {
            logger.info("User Disconnected sessionId: " + sessionId);

            itemSelectorService.unselect(sessionId);
        }
    }
}