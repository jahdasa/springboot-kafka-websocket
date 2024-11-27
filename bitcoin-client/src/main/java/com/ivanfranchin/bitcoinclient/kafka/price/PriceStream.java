package com.ivanfranchin.bitcoinclient.kafka.price;

import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
@Configuration
public class PriceStream {

    private final SimpMessagingTemplate simpMessagingTemplate;
    public static final Map<String, PriceMessage> PRICES = new ConcurrentHashMap<>();

    private final ItemSelectorService itemSelectorService;

    private ItemSelector<String> priceSelector;

    @PostConstruct
    public void postConstruct()
    {
        priceSelector = itemSelectorService.findSelectorOrNew("price");
    }

    @Bean
    public Consumer<Message<PriceMessage>> prices() {
        return message -> {

            final PriceMessage priceMessage = message.getPayload();
            final MessageHeaders messageHeaders = message.getHeaders();

            log.info(
                "PriceMessage with id {}, isin {}, value '{}' and timestamp '{}' received from bus. topic: {}, partition: {}, offset: {}, deliveryAttempt: {}",
                priceMessage.id(), priceMessage.isin() ,priceMessage.value(), priceMessage.timestamp(),
                messageHeaders.get(KafkaHeaders.RECEIVED_TOPIC, String.class),
                messageHeaders.get(KafkaHeaders.RECEIVED_PARTITION, Integer.class),
                messageHeaders.get(KafkaHeaders.OFFSET, Long.class),
                messageHeaders.get(IntegrationMessageHeaderAccessor.DELIVERY_ATTEMPT, AtomicInteger.class));


            priceSelector.getSessions(priceMessage.isin()).parallelStream()
                .forEach(session ->
                {
                    final String sessionId = (String) session.get("sessionId");
                    final String user = (String) session.get("user");

                    System.out.println("--> destination: /topic/prices, user: " + user + ", sessionId: " + sessionId);

                    simpMessagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/topic/price",
                        List.of(priceMessage),
                        createHeaders(sessionId));
                });

            PRICES.put(priceMessage.isin(), priceMessage);
        };
    }

    private MessageHeaders createHeaders(String sessionId) {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
