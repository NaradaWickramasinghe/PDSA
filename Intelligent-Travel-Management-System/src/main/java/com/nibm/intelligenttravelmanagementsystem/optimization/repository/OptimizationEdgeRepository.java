package com.nibm.intelligenttravelmanagementsystem.optimization.repository;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptimizationEdgeRepository extends JpaRepository<Edge, String> {
    List<Edge> findBySource(String source);
    List<Edge> findByDestination(String destination);
    Optional<Edge> findBySourceAndDestination(String source, String destination);
}
