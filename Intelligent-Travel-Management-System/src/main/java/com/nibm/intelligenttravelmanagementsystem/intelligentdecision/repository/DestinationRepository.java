package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID> {

    Optional<Destination> findByNameIgnoreCase(String name);

    List<Destination> findByAverageDailyCostLessThanEqualAndMinimumDaysLessThanEqual(
            BigDecimal maxDailyCost,
            Integer maxDays
    );
}
