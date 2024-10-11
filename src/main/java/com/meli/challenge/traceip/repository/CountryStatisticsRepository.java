package com.meli.challenge.traceip.repository;

import com.meli.challenge.traceip.model.CountryStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryStatisticsRepository extends JpaRepository<CountryStatistics, String> {
}
