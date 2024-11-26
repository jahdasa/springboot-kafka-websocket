package com.ivanfranchin.bitcoinclient.kafka;

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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class PriceStream {

    private final SimpMessagingTemplate simpMessagingTemplate;
    public static final Map<String, PriceMessage> PRICES = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Map<String, String>>> ISIN_SESSION_MAP = new ConcurrentHashMap<>();

    static
    {
        ISIN_SESSION_MAP.put("IRT3TVAF0001", new ConcurrentHashMap<>());
        ISIN_SESSION_MAP.put("IRO1FOLD0001", new ConcurrentHashMap<>());
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


            ISIN_SESSION_MAP.getOrDefault(priceMessage.isin(), Collections.emptyMap()).values().parallelStream()
                .forEach(session ->
                {
                    final String sessionId = session.get("sessionId");
                    final String user = session.get("user");

                    System.out.println("--> destination: /topic/prices, user: " + user + ", sessionId: " + sessionId);

//                    simpMessagingTemplate.convertAndSend(
//                            "/topic/prices",
//                            priceMessage
//                    );

                    simpMessagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/topic/prices",
                        priceMessage,
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
