package com.nibm.intelligenttravelmanagementsystem.shared.db.repositories;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, String> {
    Optional<Node> findByNodeId(String nodeId);
    boolean existsByNodeId(String nodeId);
}
