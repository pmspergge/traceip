package com.meli.challenge.traceip.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "country_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryStatistics {

    @Id
    private String countryCode;

    private double totalDistance;

    private long invocationCount;

}
