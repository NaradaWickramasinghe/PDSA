package com.nibm.intelligenttravelmanagementsystem.networkanalysis.repository;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing Edge entities from the shared 'edges' table.
 * Used by the network analysis module to load all travel routes for graph construction.
 *
 * <p>PK type is String (matches edges.edge_id VARCHAR(20) in the database).
 */
@Repository
public interface NetworkEdgeRepository extends JpaRepository<Edge, String> {
}
