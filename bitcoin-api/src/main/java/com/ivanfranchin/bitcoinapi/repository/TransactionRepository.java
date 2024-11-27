package com.ivanfranchin.bitcoinapi.repository;

import com.ivanfranchin.bitcoinapi.model.Price;
import com.ivanfranchin.bitcoinapi.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    Transaction findTopByPortfolioIdAndIsinOrderByTimestampDesc(Long portfolioId, String isin);
}
