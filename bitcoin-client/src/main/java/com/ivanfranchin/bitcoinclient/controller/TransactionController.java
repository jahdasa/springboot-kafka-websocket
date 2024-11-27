package com.ivanfranchin.bitcoinclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.bitcoinclient.kafka.transaction.TransactionMessage;
import com.ivanfranchin.bitcoinclient.kafka.transaction.TransactionStream;
import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import com.ivanfranchin.bitcoinclient.selector.SelectorFilter;
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

@RequiredArgsConstructor
@Controller
public class TransactionController {

    private final ItemSelectorService itemSelectorService;
    private final ObjectMapper mapper = new ObjectMapper();

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

        final SelectorFilter filter = transactionSelector.getFilterOrNew(sessionId);
        filter.putMetadata("username", username);

        filter.putKeyValue(message.portfolioIds(), TransactionMessage::portfolioId);

        filter.putFieldValue("types", message.types(), TransactionMessage::type);
        filter.putFieldValue("isins", message.isins(), TransactionMessage::isin);

        if ( message.portfolioIds() != null && !message.portfolioIds().isEmpty())
        {
            transactionSelector.select(sessionId, filter);

            return  message.portfolioIds().stream()
                    .filter(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId) != null)
                    .map(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId))
                    .filter(transaction -> filter.apply(transaction))
                    .toList();
        }

        return Collections.emptyList();
    }

    @MessageMapping("/transaction/remove-items")
    public void removeIsinFilter(
        @Payload final RemovePortfolioIdMessage message,
        @Header("simpSessionId") final String sessionId)
    {
        final SelectorFilter filter = transactionSelector.getFilterOrNew(sessionId);

        filter.removeKeyValue(message.portfolioIds());

        filter.removeFieldValue("types", message.types());
        filter.removeFieldValue("isins", message.isins());


        final List<Long> portfolioIds = message.portfolioIds();
        if (portfolioIds != null && !portfolioIds.isEmpty())
        {
            transactionSelector.unselect(sessionId, filter);
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

            final SelectorFilter filter = transactionSelector.getFilterOrNew(sessionId);
            filter.putMetadata("username", username);

            filter.putKeyValue(portfolioIds, TransactionMessage::portfolioId);

            filter.putFieldValue("types", types, TransactionMessage::type);
            filter.putFieldValue("isins", isins, TransactionMessage::isin);

            transactionSelector.select(sessionId, filter);

            transactions = portfolioIds.stream()
                    .filter(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId) != null)
                    .map(portfolioId -> TransactionStream.TRANSACTIONS.get(portfolioId))
                    .filter(transaction -> filter.apply(transaction))
                    .toList();

        } catch (final JsonProcessingException e)
        {
            System.out.println(e.getMessage());
        }

        return transactions;
    }
}
