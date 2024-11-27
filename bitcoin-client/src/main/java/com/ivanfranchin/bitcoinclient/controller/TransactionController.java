package com.ivanfranchin.bitcoinclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.bitcoinclient.kafka.transaction.TransactionMessage;
import com.ivanfranchin.bitcoinclient.kafka.transaction.TransactionStream;
import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import com.ivanfranchin.bitcoinclient.selector.Session;
import com.ivanfranchin.bitcoinclient.websocket.AddPortfolioIdMessage;
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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Controller
public class TransactionController {

    private final ItemSelectorService itemSelectorService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Consumer transaction;

    private ItemSelector<Long> transactionSelector;

    @PostConstruct
    public void postConstruct()
    {
        transactionSelector = itemSelectorService.findSelectorOrNew("transaction");
    }

    @MessageMapping("/transaction/add-items")
    @SendToUser("/topic/transaction")
    public List<TransactionMessage> addIsinFilter(
        @Payload final AddPortfolioIdMessage message,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId)
    {
        final String username = ((StompHeaderAccessor) accessor).getUser().getName();

        final Session session = transactionSelector.getSessionOrNew(sessionId);
        session.putMetadata("username", username);

        session.putFieldValue("portfolioIds", message.portfolioIds(), TransactionMessage::portfolioId);
        session.putFieldValue("types", message.types(), TransactionMessage::type);
        session.putFieldValue("isins", message.isins(), TransactionMessage::isin);

        if ( message.portfolioIds() != null && !message.portfolioIds().isEmpty())
        {
            transactionSelector.select(sessionId, session, message.portfolioIds());

            return  message.portfolioIds().stream()
                    .filter(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId) != null)
                    .map(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId))
                    .filter(transaction -> session.apply(transaction))
                    .toList();
        }

        return Collections.emptyList();
    }

    @MessageMapping("/transaction/remove-items")
    public void removeIsinFilter(
        @Payload final RemovePortfolioIdMessage message,
        @Header("simpSessionId") final String sessionId)
    {
        final Session session = transactionSelector.getSessionOrNew(sessionId);

        session.removeFieldValue("portfolioIds", message.portfolioIds());
        session.removeFieldValue("types", message.types());
        session.removeFieldValue("isins", message.isins());


        final List<Long> portfolioIds = message.portfolioIds();
        if (portfolioIds != null && !portfolioIds.isEmpty())
        {
            transactionSelector.unselect(sessionId, portfolioIds);
        }
    }

    @SubscribeMapping("/transaction")
    public List<TransactionMessage> handleSubscription(
        final MessageHeaders headers,
        final MessageHeaderAccessor accessor,
        @Header("simpSessionId") final String sessionId) {
        // Do some logic here when a subscription happens

        final String username = ((StompHeaderAccessor) accessor).getUser().getName();

        final String portfolioIdsHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("portfolioIds");
        final String typesHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("types");
        final String isinsHeader = ((StompHeaderAccessor) accessor).getFirstNativeHeader("isins");



        List<TransactionMessage> transactions = Collections.emptyList();

        try {
            final List<Long> portfolioIds = portfolioIdsHeader == null ? List.of() : mapper.readValue(portfolioIdsHeader, mapper.getTypeFactory().constructCollectionType(List.class, Long.class));
            final List<String> types = typesHeader == null ? List.of() :  mapper.readValue(typesHeader, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            final List<String> isins = isinsHeader == null ? List.of() : mapper.readValue(isinsHeader, mapper.getTypeFactory().constructCollectionType(List.class, String.class));

            final Session session = transactionSelector.getSessionOrNew(sessionId);
            session.putMetadata("username", username);

            session.putFieldValue("portfolioIds", portfolioIds, TransactionMessage::portfolioId);
            session.putFieldValue("types", types, TransactionMessage::type);
            session.putFieldValue("isins", isins, TransactionMessage::isin);

            transactionSelector.select(sessionId, session, portfolioIds);

            transactions = portfolioIds.stream()
                    .filter(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId) != null)
                    .map(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId))
                    .filter(transaction -> session.apply(transaction))
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
