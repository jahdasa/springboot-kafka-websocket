package com.ivanfranchin.bitcoinclient.kafka.price;

import com.ivanfranchin.bitcoinclient.selector.ItemSelector;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorService;
import com.ivanfranchin.bitcoinclient.selector.ItemSelectorStream;
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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
@Configuration
public class PriceStream {

    private final ItemSelectorService itemSelectorService;

    private final ItemSelectorStream itemSelectorStream;
    private ItemSelector<String, PriceMessage> priceSelector;


    @PostConstruct
    public void postConstruct()
    {
        priceSelector = itemSelectorService.findSelectorOrNew("price", PriceMessage::isin);
    }

    @Bean
    public Consumer<Message<PriceMessage>> prices() {
        return message -> {

            final PriceMessage priceMessage = message.getPayload();
            final MessageHeaders messageHeaders = message.getHeaders();

            log.debug(
                "PriceMessage with id {}, isin {}, value '{}' and timestamp '{}' received from bus. topic: {}, partition: {}, offset: {}, deliveryAttempt: {}",
                priceMessage.id(), priceMessage.isin() ,priceMessage.value(), priceMessage.timestamp(),
                messageHeaders.get(KafkaHeaders.RECEIVED_TOPIC, String.class),
                messageHeaders.get(KafkaHeaders.RECEIVED_PARTITION, Integer.class),
                messageHeaders.get(KafkaHeaders.OFFSET, Long.class),
                messageHeaders.get(IntegrationMessageHeaderAccessor.DELIVERY_ATTEMPT, AtomicInteger.class));

            itemSelectorStream.send("/topic/price", priceMessage, priceSelector);
        };
    }

    private MessageHeaders createHeaders(String sessionId) {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
