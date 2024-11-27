package com.ivanfranchin.bitcoinapi.service;

import com.ivanfranchin.bitcoinapi.model.Price;
import com.ivanfranchin.bitcoinapi.model.Transaction;
import com.ivanfranchin.bitcoinapi.repository.PriceRepository;
import com.ivanfranchin.bitcoinapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Transaction getLastTransaction(Long portfolioId, String isin) {
        return transactionRepository.findTopByPortfolioIdAndIsinOrderByTimestampDesc(portfolioId, isin);
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
