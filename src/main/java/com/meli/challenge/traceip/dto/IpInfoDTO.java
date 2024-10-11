package com.meli.challenge.traceip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IpInfoDTO {

    private String ip;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("country_code")
    private String countryCode;

    private String city;

    @JsonProperty("region")
    private String regionName;

    private Double latitude;
    private Double longitude;

    @JsonProperty("currency")
    private String currencyCode;

    private Double distanceToBA;

    private String formattedCurrentDate;
}
