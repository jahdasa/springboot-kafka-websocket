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
    private final Map<Long, String> numberToIsin = Map.of(
            0L, "IRT3TVAF0001" ,
            1L, "IRO1FOLD0001"
    );

    public void send(Price price) {

        final String isin = numberToIsin.get(price.getId() % 2);

        final PriceMessage priceMessage = new PriceMessage(price.getId(), isin, price.getValue(), price.getTimestamp());
        streamBridge.send("prices-out-0", priceMessage);

        log.info("{} sent to bus.", priceMessage);
    }
}
