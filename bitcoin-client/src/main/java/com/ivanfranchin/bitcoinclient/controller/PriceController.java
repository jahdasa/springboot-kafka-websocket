package com.ivanfranchin.bitcoinclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import com.ivanfranchin.bitcoinclient.kafka.price.PriceMessage;
import com.ivanfranchin.bitcoinclient.selector.SelectorFilter;
import com.ivanfranchin.bitcoinclient.websocket.AddIsinMessage;
import com.ivanfranchin.bitcoinclient.websocket.RemoveIsinMessage;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PriceController {

    private final ItemSelectorService itemSelectorService;
    private final ObjectMapper mapper = new ObjectMapper();

    private ItemSelector<String, PriceMessage> priceSelector;

    @PostConstruct
    public void postConstruct()
    {
        priceSelector = itemSelectorService.findSelectorOrNew("price", PriceMessage::isin);
    }

    @GetMapping("/")
    public String getPrices() {
        return "prices";
    }

    @Timed
    @MessageMapping("/price/add-filter")
    @SendToUser("/topic/price")
    public List<PriceMessage> addFilter(
        @Payload final AddIsinMessage message,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId)
    {
        final String username = "user1"; //((StompHeaderAccessor) accessor).getUser().getName();

        final SelectorFilter filter = priceSelector.getFilterOrNew(sessionId);
        filter.putMetadata("username", username);

        filter.putKeyValue(message.isins());

        log.info("username: {}, add-filter: {}", username, message);
        final List<String> isins = message.isins();
        if (!isins.isEmpty())
        {
            priceSelector.select(sessionId, filter);

            return priceSelector.getData(filter);
        }

        return Collections.emptyList();
    }

    @MessageMapping("/price/remove-filter")
    public void removeFilter(
        @Payload final RemoveIsinMessage message,
        @Header("simpSessionId") final String sessionId)
    {
        final List<String> isins = message.isins();

        final SelectorFilter filter = priceSelector.getFilterOrNew(sessionId);

        filter.removeKeyValue(message.isins());

        if (!isins.isEmpty())
        {
            priceSelector.unselect(sessionId, filter);
        }
    }

    @SubscribeMapping("/price")
    public List<PriceMessage> handleSubscription(
        final MessageHeaders headers,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId) {
        // Do some logic here when a subscription happens

        final String username = "user1"; //((StompHeaderAccessor) accessor).getUser().getName();

        String isinsHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("isins");
        if (isinsHeader == null)
        {
            isinsHeader = "[]";
        }
        List<PriceMessage> prices = Collections.emptyList();

        try {
            final List<String> isins = mapper.readValue(isinsHeader, List.class);

            final SelectorFilter filter = priceSelector.getFilterOrNew(sessionId);
            filter.putMetadata("username", username);

            filter.putKeyValue(isins);

            priceSelector.select(sessionId, filter);

            prices = priceSelector.getData(filter);
        } catch (final JsonProcessingException e)
        {
            System.out.println(e.getMessage());
        }

        return prices;
    }
}
