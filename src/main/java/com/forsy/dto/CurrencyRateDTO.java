package com.forsy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyRateDTO {

    @JsonProperty("ccy")
    private String currency; // EUR, USD

    @JsonProperty("base_ccy")
    private String baseCurrency; // UAH

    private BigDecimal buy;
    private BigDecimal sale;
}
