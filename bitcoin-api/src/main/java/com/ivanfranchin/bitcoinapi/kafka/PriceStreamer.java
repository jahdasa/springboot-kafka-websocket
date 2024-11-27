package com.ivanfranchin.bitcoinapi.kafka;

import com.ivanfranchin.bitcoinapi.model.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class PriceStreamer {

    private final StreamBridge streamBridge;


    public void send(Price price) {

        final PriceMessage priceMessage = new PriceMessage(price.getId(), price.getIsin(), price.getValue(), price.getTimestamp());
        streamBridge.send("prices-out-0", priceMessage);

        log.info("{} sent to bus.", priceMessage);
    }
}
