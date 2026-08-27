package com.nibm.intelligenttravelmanagementsystem.routeoptimization.repository;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, Long> {

    // Find by String edgeId
    Optional<Edge> findByEdgeId(String edgeId);

    // Find by source node ID (Long)
    List<Edge> findBySourceNodeId(Long sourceNodeId);

    // Find by target node ID (Long)
    List<Edge> findByTargetNodeId(Long targetNodeId);

    // Find by source (String nodeId)
    List<Edge> findBySource(String source);

    // Find by destination (String nodeId)
    List<Edge> findByDestination(String destination);

    // Find all edges connected to a node
    @Query("SELECT e FROM Edge e WHERE e.sourceNodeId = :nodeId OR e.targetNodeId = :nodeId")
    List<Edge> findConnectedEdges(@Param("nodeId") Long nodeId);

    // Find edge between two nodes
    @Query("SELECT e FROM Edge e WHERE (e.sourceNodeId = :sourceId AND e.targetNodeId = :targetId) OR (e.sourceNodeId = :targetId AND e.targetNodeId = :sourceId)")
    Optional<Edge> findEdgeBetweenNodes(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    // Find by transport mode
    List<Edge> findByTransportMode(String transportMode);

    // Find by risk level less than or equal
    List<Edge> findByRiskLevelLessThanEqual(Short maxRiskLevel);
}