package com.ivanfranchin.bitcoinapi.service;

import com.ivanfranchin.bitcoinapi.model.Price;
import com.ivanfranchin.bitcoinapi.model.Transaction;

public interface TransactionService {

    Transaction getLastTransaction(Long portfolioId, String isin);

    Transaction saveTransaction(Transaction transaction);
}
