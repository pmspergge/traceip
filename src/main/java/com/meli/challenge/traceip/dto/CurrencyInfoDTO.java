package com.meli.challenge.traceip.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CurrencyInfoDTO {
    private String base;
    private Map<String, Double> rates;
    private Double conversionRateToUSD;
    private Double conversionRateToARS;
}
