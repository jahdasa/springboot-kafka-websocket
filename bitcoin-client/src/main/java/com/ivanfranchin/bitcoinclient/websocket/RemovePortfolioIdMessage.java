package com.ivanfranchin.bitcoinclient.websocket;

import java.util.List;

public record RemovePortfolioIdMessage(List<Long> portfolioIds, List<String> types, List<String> isins) {
}
