package com.ivanfranchin.bitcoinapi.runner;

import com.ivanfranchin.bitcoinapi.model.Price;
import com.ivanfranchin.bitcoinapi.model.Transaction;
import com.ivanfranchin.bitcoinapi.service.PriceService;
import com.ivanfranchin.bitcoinapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class SimulationRunner implements CommandLineRunner {

    private final PriceService priceService;
    private final TransactionService transactionService;

    @Override
    public void run(String... args) {
        priceService.savePrice(new Price(BigDecimal.valueOf(17000), "IRT3TVAF0001", LocalDateTime.now()));
        priceService.savePrice(new Price(BigDecimal.valueOf(30000), "IRO1FOLD0001", LocalDateTime.now()));


        transactionService.saveTransaction(new Transaction(
                1270L,
                "IRT3TVAF0001" ,
                "BUY",
                100L,
                BigDecimal.valueOf(17000),
                BigDecimal.valueOf(17000).multiply(BigDecimal.valueOf(100)),
                LocalDateTime.now()));

        transactionService.saveTransaction(new Transaction(
                1270L,
                "IRO1FOLD0001" ,
                "BUY",
                100L,
                BigDecimal.valueOf(30000),
                BigDecimal.valueOf(30000).multiply(BigDecimal.valueOf(100)),
                LocalDateTime.now()));


        transactionService.saveTransaction(new Transaction(
                1271L,
                "IRT3TVAF0001" ,
                "BUY",
                100L,
                BigDecimal.valueOf(17000),
                BigDecimal.valueOf(17000).multiply(BigDecimal.valueOf(100)),
                LocalDateTime.now()));

        transactionService.saveTransaction(new Transaction(
                1271L,
                "IRO1FOLD0001" ,
                "BUY",
                100L,
                BigDecimal.valueOf(30000),
                BigDecimal.valueOf(30000).multiply(BigDecimal.valueOf(100)),
                LocalDateTime.now()));
    }
}
