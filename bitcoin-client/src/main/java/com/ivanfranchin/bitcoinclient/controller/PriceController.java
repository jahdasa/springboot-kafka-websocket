package com.ivanfranchin.bitcoinclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.bitcoinclient.kafka.PriceMessage;
import com.ivanfranchin.bitcoinclient.kafka.PriceStream;
import com.ivanfranchin.bitcoinclient.websocket.AddIsinMessage;
import com.ivanfranchin.bitcoinclient.websocket.ChatMessage;
import com.ivanfranchin.bitcoinclient.websocket.RemoveIsinMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class PriceController {

    private final ObjectMapper mapper = new ObjectMapper();

    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/")
    public String getPrices() {
        return "prices";
    }

    @MessageMapping("/chat")
    public void addChatComment(@Payload ChatMessage chatMessage) {
        if (chatMessage.toUser().isEmpty()) {
            simpMessagingTemplate.convertAndSend("/topic/chat-messages", chatMessage);
        } else {
            simpMessagingTemplate.convertAndSendToUser(chatMessage.toUser(), "/topic/chat-messages", chatMessage);
        }
    }

    @MessageMapping("/prices/add-isins")
    public void addIsinFilter(
        @Payload final AddIsinMessage message,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId)
    {
        final String userName = ((StompHeaderAccessor) accessor).getUser().getName();

        if (!message.isins().isEmpty())
        {
            message.isins().stream()
                .forEach(isin ->
                {
                    final Map<String, Map<String, String>> sessionsMap = PriceStream.ISIN_SESSION_MAP.get(isin);

                    final Map<String, String> session = new HashMap<>();
                    session.put("sessionId", sessionId);
                    session.put("user", userName);

                    sessionsMap.put(sessionId, session);
                });
        }
    }

    @MessageMapping("/prices/remove-isins")
    public void removeIsinFilter(
        @Payload final RemoveIsinMessage message,
        @Header("simpSessionId") final String sessionId)
    {

        if (!message.isins().isEmpty())
        {
            message.isins().stream()
                .forEach(isin ->
                {
                    final Map<String, Map<String, String>> sessionsMap = PriceStream.ISIN_SESSION_MAP.get(isin);

                    sessionsMap.remove(sessionId);
                });
        }
    }

    @SubscribeMapping("/prices")
    public List<PriceMessage> handleSubscription(
        final MessageHeaders headers,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId) {
        // Do some logic here when a subscription happens

        final String userName = ((StompHeaderAccessor) accessor).getUser().getName();

        final String isinsHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("isins");

        List<PriceMessage> prices = Collections.emptyList();

        try {
            final List<String> isins = mapper.readValue(isinsHeader, List.class);

            isins.stream()
                .forEach(isin ->
                {
                    final Map<String, Map<String, String>> sessionsMap = PriceStream.ISIN_SESSION_MAP.get(isin);

                    final Map<String, String> session = new HashMap<>();
                    session.put("sessionId", sessionId);
                    session.put("user",userName);

                    sessionsMap.put(sessionId, session);
                });


            prices = isins.stream()
                    .filter(isin -> PriceStream.PRICES.get(isin) != null)
                    .map(isin -> PriceStream.PRICES.get(isin))
                    .toList();

        } catch (final JsonProcessingException e)
        {
            System.out.println(e.getMessage());
        }

        return prices;
    }
}
