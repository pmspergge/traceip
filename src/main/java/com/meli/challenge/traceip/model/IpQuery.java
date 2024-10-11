package com.meli.challenge.traceip.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ip_queries")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ip;

    private String countryName;

    private String countryCode;

    private double distanceToBA;

    private LocalDateTime queryTime;

}

