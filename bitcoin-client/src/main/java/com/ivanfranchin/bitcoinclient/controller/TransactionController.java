package com.ivanfranchin.bitcoinclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.bitcoinclient.kafka.transaction.TransactionMessage;
import com.ivanfranchin.bitcoinclient.kafka.transaction.TransactionStream;
import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import com.ivanfranchin.bitcoinclient.kafka.price.PriceMessage;
import com.ivanfranchin.bitcoinclient.kafka.price.PriceStream;
import com.ivanfranchin.bitcoinclient.websocket.AddIsinMessage;
import com.ivanfranchin.bitcoinclient.websocket.AddPortfolioIdMessage;
import com.ivanfranchin.bitcoinclient.websocket.RemoveIsinMessage;
import com.ivanfranchin.bitcoinclient.websocket.RemovePortfolioIdMessage;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class TransactionController {

    private final ItemSelectorService itemSelectorService;
    private final ObjectMapper mapper = new ObjectMapper();

    private ItemSelector<Long> portfolioSelector;

    @PostConstruct
    public void postConstruct()
    {
        portfolioSelector = itemSelectorService.findSelectorOrNew("portfolio");
    }

    @MessageMapping("/transaction/add-items")
    @SendToUser("/topic/transaction")
    public List<TransactionMessage> addIsinFilter(
        @Payload final AddPortfolioIdMessage message,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId)
    {
        final String userName = ((StompHeaderAccessor) accessor).getUser().getName();

        final Map<String, Object> session = portfolioSelector.getSessionOrNew(sessionId);
        final Map<Long, Long> portfolioIdMap = (Map<Long, Long>) session.get("portfolioIds");
        final Map<String, String> typeMap = (Map<String, String>) session.get("types");
        final Map<String, String> isinMap = (Map<String, String>) session.get("isins");


        if(message.isins() != null)
        {
            message.isins().stream()
                .forEach(isin -> isinMap.put(isin, isin));
        }

        if(message.types() != null)
        {
            message.types().stream()
                .forEach(type -> typeMap.put(type, type));
        }

        final List<Long> portfolioIds = message.portfolioIds();

        if (portfolioIds != null && !portfolioIds.isEmpty())
        {
            portfolioIds.stream()
                    .forEach(portfolioId -> portfolioIdMap.put(portfolioId, portfolioId));

            portfolioSelector.select(sessionId, session, portfolioIds);

            return portfolioIds.stream()
                    .filter(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId) != null)
                    .map(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId))
                    .filter(transaction -> typeMap.isEmpty() || typeMap.containsKey(transaction.type()))
                    .filter(transaction -> isinMap.isEmpty() || isinMap.containsKey(transaction.isin()))
                    .toList();
        }

        return Collections.emptyList();
    }

    @MessageMapping("/transaction/remove-items")
    public void removeIsinFilter(
        @Payload final RemovePortfolioIdMessage message,
        @Header("simpSessionId") final String sessionId)
    {
        final Map<String, Object> session = portfolioSelector.getSessionOrNew(sessionId);
        final Map<Long, Long> portfolioIdMap = (Map<Long, Long>) session.get("portfolioIds");
        final Map<String, String> typeMap = (Map<String, String>) session.get("types");
        final Map<String, String> isinMap = (Map<String, String>) session.get("isins");

        final List<Long> portfolioIds = message.portfolioIds();
        if (portfolioIds != null && !portfolioIds.isEmpty())
        {
            portfolioIds.stream()
                    .forEach(portfolioId -> portfolioIdMap.remove(portfolioId));

            portfolioSelector.unselect(sessionId, portfolioIds);
        }

        if(message.isins() != null)
        {
            message.isins().stream()
                .forEach(isin -> isinMap.remove(isin));
        }

        if(message.types() != null)
        {
            message.types().stream()
                    .forEach(type -> typeMap.remove(type));
        }
    }

    @SubscribeMapping("/transaction")
    public List<TransactionMessage> handleSubscription(
        final MessageHeaders headers,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId) {
        // Do some logic here when a subscription happens

        final String userName = ((StompHeaderAccessor) accessor).getUser().getName();

        final String portfolioIdsHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("portfolioIds");
        final String typesHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("types");
        final String isinsHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("isins");

        List<TransactionMessage> transactions = Collections.emptyList();

        try {
            final List<Long> portfolioIds = portfolioIdsHeader == null ? List.of() : mapper.readValue(portfolioIdsHeader, mapper.getTypeFactory().constructCollectionType(List.class, Long.class));
            final List<String> types = typesHeader == null ? List.of() :  mapper.readValue(typesHeader, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            final List<String> isins = isinsHeader == null ? List.of() : mapper.readValue(isinsHeader, mapper.getTypeFactory().constructCollectionType(List.class, String.class));

            final Map<String, Object> session = portfolioSelector.getSessionOrNew(sessionId);

            Map<Long, Long> portfolioIdMap = portfolioIds != null ? portfolioIds.stream().collect(Collectors.toMap(portfolioId -> portfolioId, portfolioId -> portfolioId)) : Collections.emptyMap();
            Map<String, String> typeMap = types != null ? types.stream().collect(Collectors.toMap(type -> type, type -> type)) : Collections.emptyMap();
            Map<String, String> isinMap = isins != null ? isins.stream().collect(Collectors.toMap(isin -> isin, isin -> isin)) : Collections.emptyMap();

            session.put("user",userName);
            session.put("portfolioIds", portfolioIdMap);
            session.put("types", typeMap);
            session.put("isins", isinMap);

            portfolioSelector.select(sessionId, session, portfolioIds);

            transactions = portfolioIds.stream()
                    .filter(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId) != null)
                    .map(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId))
                    .filter(transaction -> typeMap.isEmpty() || typeMap.containsKey(transaction.type()))
                    .filter(transaction -> isinMap.isEmpty() || isinMap.containsKey(transaction.isin()))
                    .toList();

        } catch (final JsonProcessingException e)
        {
            System.out.println(e.getMessage());
        }

        return transactions;
    }

    private MessageHeaders createHeaders(String sessionId)
    {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
