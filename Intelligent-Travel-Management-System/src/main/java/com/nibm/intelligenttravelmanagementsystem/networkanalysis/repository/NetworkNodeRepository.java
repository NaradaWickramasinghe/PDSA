package com.nibm.intelligenttravelmanagementsystem.networkanalysis.repository;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing Node entities from the shared 'nodes' table.
 * Used by the network analysis module to load all destinations for graph construction.
 *
 * <p>PK type is String (matches nodes.node_id VARCHAR(20) in the database).
 */
@Repository
public interface NetworkNodeRepository extends JpaRepository<Node, String> {
}
