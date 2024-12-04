package com.ivanfranchin.bitcoinclient.kafka.transaction;

import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorStream;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionStream {

    private final ItemSelectorService itemSelectorService;
    private final ItemSelectorStream itemSelectorStream;

    private ItemSelector<Long, TransactionMessage> portfolioSelector;

    @PostConstruct
    public void postConstruct()
    {
        portfolioSelector = itemSelectorService.findSelectorOrNew("transaction", TransactionMessage::portfolioId);
    }

    @Bean
    public Consumer<Message<TransactionMessage>> transaction() {
        return message -> {

            final TransactionMessage transactionMessage = message.getPayload();
            final MessageHeaders messageHeaders = message.getHeaders();

            log.debug(
                "TransactionMessage with id {}, portfolioId: {}, isin {}, type: {}, count: {}, price: {} value '{}' and timestamp '{}' received from bus. topic: {}, partition: {}, offset: {}, deliveryAttempt: {}",
                transactionMessage.id(),
                transactionMessage.portfolioId(),
                transactionMessage.isin() ,
                transactionMessage.type(),
                transactionMessage.count(),
                transactionMessage.price(),
                transactionMessage.value(),
                transactionMessage.timestamp(),

                messageHeaders.get(KafkaHeaders.RECEIVED_TOPIC, String.class),
                messageHeaders.get(KafkaHeaders.RECEIVED_PARTITION, Integer.class),
                messageHeaders.get(KafkaHeaders.OFFSET, Long.class),
                messageHeaders.get(IntegrationMessageHeaderAccessor.DELIVERY_ATTEMPT, AtomicInteger.class));

            itemSelectorStream.send("/topic/transaction", transactionMessage, portfolioSelector);
        };
    }

    private MessageHeaders createHeaders(String sessionId) {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
