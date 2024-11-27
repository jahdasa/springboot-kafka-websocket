package com.ivanfranchin.bitcoinclient.selector;

import com.ivanfranchin.bitcoinclient.kafka.price.PriceMessage;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
@Configuration
public class ItemSelectorStream {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public <T,U> void send(final String destination, final U message, final ItemSelector<T, U> itemSelector)
    {
        final T value = itemSelector.getKeyMapper().apply(message);
        itemSelector.getFilters(value).parallelStream()
                .forEach(filter ->
                {
                    final String sessionId = filter.getSessionId();
                    final String user =  (String) filter.getMetadata("username");

                    System.out.println("--> destination: /topic/prices, user: " + user + ", sessionId: " + sessionId);

                    simpMessagingTemplate.convertAndSendToUser(
                        sessionId,
                            destination,
                        List.of(message),
                        createHeaders(sessionId));
                });

            itemSelector.putData(value, message);
    }

    private MessageHeaders createHeaders(String sessionId) {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
