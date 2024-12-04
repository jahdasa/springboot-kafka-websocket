package com.ivanfranchin.bitcoinapi.scheduler;

import com.ivanfranchin.bitcoinapi.kafka.PriceStreamer;
import com.ivanfranchin.bitcoinapi.kafka.TransactionStreamer;
import com.ivanfranchin.bitcoinapi.model.Price;
import com.ivanfranchin.bitcoinapi.model.Transaction;
import com.ivanfranchin.bitcoinapi.service.PriceService;
import com.ivanfranchin.bitcoinapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Component
public class PriceScheduler {

    private final PriceService priceService;
    private final TransactionService transactionService;
    private final PriceStreamer priceStreamer;
    private final TransactionStreamer transactionStreamer;

    private final Map<Long, String> numberToIsin = Map.of(
            0L, "IRT3TVAF0001" ,
            1L, "IRO1FOLD0001"
    );

    private final Map<Long, Long> numberToPortfolioId = Map.of(
            0L, 1270L ,
            1L, 1271L
    );

    @Scheduled(cron = "*/1 * * * * *") // every 2 seconds
    public void streamNewPrice() {
        if (hasTrade()) {
            for (int i = 0; i < 10; i++) {
                sendTrade();
            }

        }
    }

    private void sendTrade() {
        final String isin = numberToIsin.get(nextIsin());

        Price price = priceService.getLastPrice(isin);
        price = getNewPrice(isin, price.getValue());
        priceService.savePrice(price);

        final Long portfolioId = numberToPortfolioId.get(nextPortfolioId());

        Transaction transaction = transactionService.getLastTransaction(portfolioId, isin);
        transaction = getNewTransaction(transaction, price.getValue());
        transactionService.saveTransaction(transaction);

        priceStreamer.send(price);
        transactionStreamer.send(transaction);
    }

    private boolean hasTrade() {
        return true;//rand.nextBoolean();
    }

    private Long nextIsin() {
        return rand.nextLong(numberToIsin.size());
    }

    private Long nextPortfolioId() {
        return rand.nextLong(numberToPortfolioId.size());
    }

    private Long nextCount() {
        return rand.nextLong(1000);
    }

    private Price getNewPrice(String isin, BigDecimal currentPrice) {
        boolean sign = rand.nextBoolean();
        double var = rand.nextDouble() * 100;
        BigDecimal variation = BigDecimal.valueOf(sign ? var : -1 * var);
        BigDecimal newValue = currentPrice.add(variation).setScale(2, RoundingMode.HALF_UP);
        return new Price(newValue, isin, LocalDateTime.now());
    }

    private Transaction getNewTransaction(Transaction transaction, BigDecimal price) {
        boolean sign = rand.nextBoolean();
        final Long countVar = Math.abs(rand.nextLong(100 + Math.abs(transaction.getCount())));

        String type = "BUY";
        if(sign)
        {
            type = "SELL";
        }
        return new Transaction(
                transaction.getPortfolioId(),
                transaction.getIsin(),
                type,
                countVar,
                price,
                price.multiply(BigDecimal.valueOf(countVar)),
                LocalDateTime.now()
                );
    }

    private static final Random rand = new SecureRandom();
}
