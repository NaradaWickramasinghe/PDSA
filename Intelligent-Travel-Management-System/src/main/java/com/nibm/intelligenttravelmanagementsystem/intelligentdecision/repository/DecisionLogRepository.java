package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.DecisionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DecisionLogRepository extends JpaRepository<DecisionLog, UUID> {

    List<DecisionLog> findByTravelerIdOrderByCreatedAtDesc(UUID travelerId);

    Page<DecisionLog> findByTravelerIdOrderByCreatedAtDesc(UUID travelerId, Pageable pageable);
}
