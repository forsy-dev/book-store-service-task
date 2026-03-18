package com.forsy.service.impl;

import com.forsy.dto.CurrencyRateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

    private final RestTemplate restTemplate;
    private static final String PRIVAT_API_URL = "https://api.privatbank.ua/p24api/pubinfo?exchange&json&coursid=11";

    public List<CurrencyRateDTO> getExchangeRates() {
        try {
            CurrencyRateDTO[] rates = restTemplate.getForObject(PRIVAT_API_URL, CurrencyRateDTO[].class);
            return rates != null ? Arrays.asList(rates) : List.of();
        } catch (Exception e) {
            log.error("Failed to fetch currency rates from PrivatBank", e);
            return List.of();
        }
    }

    public BigDecimal convertUsdToUah(BigDecimal amountInUsd) {
        if (amountInUsd == null || amountInUsd.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        List<CurrencyRateDTO> rates = getExchangeRates();
        Optional<CurrencyRateDTO> rateOpt = rates.stream()
            .filter(r -> r.getCurrency().equalsIgnoreCase("USD"))
            .findFirst();

        if (rateOpt.isPresent()) {
            BigDecimal saleRate = rateOpt.get().getSale();
            return amountInUsd.multiply(saleRate).setScale(2, RoundingMode.HALF_UP);
        }

        return null;
    }
}
