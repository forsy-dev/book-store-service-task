package com.forsy.service.impl;

import com.forsy.dto.CurrencyRateDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for retrieving real-time exchange rates and performing currency conversions.
 *
 * <p>This service acts as an external integration layer, fetching current
 * financial data from the PrivatBank API to support multi-currency
 * price calculations within the bookstore.
 *
 * @author Illia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

  private final RestTemplate restTemplate;

  /**
   * The public API endpoint for PrivatBank's real-time exchange rates.
   */
  private static final String PRIVAT_API_URL =
      "https://api.privatbank.ua/p24api/pubinfo?exchange&json&coursid=11";

  /**
   * Fetches the latest exchange rates from the PrivatBank public API.
   *
   * <p>If the external service is unavailable or returns an error, the method
   * logs the failure and returns an empty list to prevent system-wide crashes.
   *
   * @return a {@link List} of {@link CurrencyRateDto} containing current rates,
   *     or an empty list if the fetch fails
   */
  public List<CurrencyRateDto> getExchangeRates() {
    try {
      CurrencyRateDto[] rates = restTemplate.getForObject(
          PRIVAT_API_URL, CurrencyRateDto[].class);
      return rates != null ? Arrays.asList(rates) : List.of();
    } catch (Exception e) {
      log.error("Failed to fetch currency rates from PrivatBank", e);
      return List.of();
    }
  }

  /**
   * Converts a specified amount from US Dollars (USD) to Ukrainian Hryvnia (UAH).
   *
   * <p>The conversion utilizes the current 'sale' rate retrieved from the
   * exchange rate service and rounds the result to two decimal places.
   *
   * @param amountInUsd the amount in USD to be converted
   * @return the converted amount in UAH as a {@link BigDecimal},
   *     or {@code null} if the USD exchange rate cannot be retrieved
   */
  public BigDecimal convertUsdToUah(BigDecimal amountInUsd) {
    if (amountInUsd == null || amountInUsd.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    List<CurrencyRateDto> rates = getExchangeRates();
    Optional<CurrencyRateDto> rateOpt = rates.stream()
        .filter(r -> r.getCurrency().equalsIgnoreCase("USD"))
        .findFirst();

    if (rateOpt.isPresent()) {
      BigDecimal saleRate = rateOpt.get().getSale();
      return amountInUsd.multiply(saleRate)
          .setScale(2, RoundingMode.HALF_UP);
    }

    return null;
  }
}
