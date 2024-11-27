package com.ivanfranchin.bitcoinapi.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionMessage(Long id, Long portfolioId, String isin , String type, Long count, BigDecimal price, BigDecimal value, LocalDateTime timestamp) {
}
