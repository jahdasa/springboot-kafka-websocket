package com.ivanfranchin.bitcoinapi.kafka;

import com.ivanfranchin.bitcoinapi.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionStreamer {

    private final StreamBridge streamBridge;


    public void send(Transaction transaction) {

        final TransactionMessage transactionMessage = new TransactionMessage(
                transaction.getId(),
                transaction.getPortfolioId(),
                transaction.getIsin(),
                transaction.getType(),
                transaction.getCount(),
                transaction.getPrice(),
                transaction.getValue(),
                transaction.getTimestamp());
        streamBridge.send("transaction-out-0", transactionMessage);

        log.info("{} sent to bus.", transaction);
    }
}
