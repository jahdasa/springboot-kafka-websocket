package com.ivanfranchin.bitcoinclient.kafka.transaction;

import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionStream {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ItemSelectorService itemSelectorService;

    private ItemSelector<Long, TransactionMessage> portfolioSelector;

    @PostConstruct
    public void postConstruct()
    {
        portfolioSelector = itemSelectorService.findSelectorOrNew("transaction");
    }

    @Bean
    public Consumer<Message<TransactionMessage>> transaction() {
        return message -> {

            final TransactionMessage transactionMessage = message.getPayload();
            final MessageHeaders messageHeaders = message.getHeaders();

            log.info(
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

            portfolioSelector.getFilters(transactionMessage.portfolioId()).parallelStream()
                .forEach(filter ->
                {
                    final String sessionId = filter.getSessionId();
                    final String user =  (String) filter.getMetadata("username");

                    System.out.println("--> destination: /topic/transaction, user: " + user + ", sessionId: " + sessionId);

                    if(filter.apply(transactionMessage))
                    {
                        simpMessagingTemplate.convertAndSendToUser(
                                sessionId,
                                "/topic/transaction",
                                List.of(transactionMessage),
                                createHeaders(sessionId));
                    }

                });

            portfolioSelector.putData(transactionMessage.portfolioId(), transactionMessage);
        };
    }

    private MessageHeaders createHeaders(String sessionId) {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
