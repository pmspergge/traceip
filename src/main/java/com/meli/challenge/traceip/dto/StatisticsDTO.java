package com.meli.challenge.traceip.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsDTO {
    private double maxDistance;
    private double minDistance;
    private double avgDistance;
}
