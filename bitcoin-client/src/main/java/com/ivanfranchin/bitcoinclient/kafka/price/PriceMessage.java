package com.ivanfranchin.bitcoinclient.kafka.price;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceMessage(Long id, String isin, BigDecimal value, LocalDateTime timestamp) {
}
