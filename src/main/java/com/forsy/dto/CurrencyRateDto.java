package com.forsy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Data transfer object representing the exchange rate for a specific currency pair.
 *
 * <p>This DTO is typically used to map responses from external banking APIs.
 * It provides the purchase and sale prices of foreign currencies relative to
 * the application's base currency.
 *
 * @author Illia
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyRateDto {

  /**
   * The code of the foreign currency (e.g., "USD", "EUR").
   */
  @JsonProperty("ccy")
  private String currency;

  /**
   * The code of the national base currency (e.g., "UAH").
   */
  @JsonProperty("base_ccy")
  private String baseCurrency;

  /**
   * The rate at which the bank buys the foreign currency.
   */
  private BigDecimal buy;

  /**
   * The rate at which the bank sells the foreign currency.
   */
  private BigDecimal sale;
}
