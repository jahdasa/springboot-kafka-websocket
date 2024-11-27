package com.ivanfranchin.bitcoinclient.websocket;

import java.util.List;

public record AddPortfolioIdMessage(List<Long> portfolioIds, List<String> types, List<String> isins) {
}
