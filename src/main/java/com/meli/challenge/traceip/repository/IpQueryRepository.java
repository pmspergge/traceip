package com.meli.challenge.traceip.repository;

import com.meli.challenge.traceip.model.IpQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IpQueryRepository extends JpaRepository<IpQuery, Long> {
    @Query("SELECT MAX(i.distanceToBA) FROM IpQuery i")
    Double findMaxDistance();

    @Query("SELECT MIN(i.distanceToBA) FROM IpQuery i")
    Double findMinDistance();
}
