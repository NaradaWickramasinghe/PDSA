package com.nibm.intelligenttravelmanagementsystem.optimization.repository;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OptimizationNodeRepository extends JpaRepository<Node, String> {
    Optional<Node> findByNodeId(String nodeId);
    boolean existsByNodeId(String nodeId);
}
