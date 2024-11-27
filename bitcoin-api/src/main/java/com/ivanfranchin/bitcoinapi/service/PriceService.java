package com.ivanfranchin.bitcoinapi.service;

import com.ivanfranchin.bitcoinapi.model.Price;

public interface PriceService {

    Price getLastPrice(String isin);

    Price savePrice(Price price);
}
