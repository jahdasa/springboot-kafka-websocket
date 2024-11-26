package com.ivanfranchin.bitcoinclient.websocket;

import java.time.Instant;
import java.util.List;

public record PricesMessage(List<String> isins) {
}
