package com.meli.challenge.traceip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.List;

@Data
@Builder
public class CountryInfoDTO {
    private Name name;
    private List<String> timezones;
    private List<String> currentTimes;

    @JsonProperty("languages")
    private Map<String, String> languages;

    @JsonProperty("currencies")
    private Map<String, Currency> currencies;

    @Data
    public static class Name {
        private String common;
        private String official;
    }

    @Data
    public static class Currency {
        private String name;
        private String symbol;
    }
}
