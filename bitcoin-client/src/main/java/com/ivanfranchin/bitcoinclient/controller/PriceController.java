package com.ivanfranchin.bitcoinclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import com.ivanfranchin.bitcoinclient.kafka.price.PriceMessage;
import com.ivanfranchin.bitcoinclient.kafka.price.PriceStream;
import com.ivanfranchin.bitcoinclient.websocket.AddIsinMessage;
import com.ivanfranchin.bitcoinclient.websocket.RemoveIsinMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class PriceController {

    private final ItemSelectorService itemSelectorService;
    private final ObjectMapper mapper = new ObjectMapper();

    private ItemSelector priceSelector;

    @PostConstruct
    public void postConstruct()
    {
        priceSelector = itemSelectorService.findSelectorOrNew("price");
    }

    @GetMapping("/")
    public String getPrices() {
        return "prices";
    }

    @MessageMapping("/price/add-items")
    @SendToUser("/topic/price")
    public List<PriceMessage> addIsinFilter(
        @Payload final AddIsinMessage message,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId)
    {
        final String userName = ((StompHeaderAccessor) accessor).getUser().getName();

        final Map<String, String> session = new HashMap<>();
        session.put("sessionId", sessionId);
        session.put("user", userName);

        final List<String> isins = message.isins();
        if (!isins.isEmpty())
        {
            priceSelector.select(sessionId, session, isins);
        }

        return isins.stream()
                .filter(isin -> PriceStream.PRICES.get(isin) != null)
                .map(isin -> PriceStream.PRICES.get(isin))
                .toList();
    }

    @MessageMapping("/price/remove-items")
    public void removeIsinFilter(
        @Payload final RemoveIsinMessage message,
        @Header("simpSessionId") final String sessionId)
    {

        final List<String> isins = message.isins();
        if (!isins.isEmpty())
        {
            priceSelector.unselect(sessionId, isins);
        }
    }

    @SubscribeMapping("/price")
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

            final Map<String, String> session = new HashMap<>();
            session.put("sessionId", sessionId);
            session.put("user",userName);

            priceSelector.select(sessionId, session, isins);

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

    private MessageHeaders createHeaders(String sessionId)
    {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
