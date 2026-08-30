package com.nibm.intelligenttravelmanagementsystem.routeoptimization.repository;

import com.nibm.intelligenttravelmanagementsystem.shared.db.models.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

    // Find by String nodeId
    Optional<Node> findByNodeId(String nodeId);

    // Find by name
    Optional<Node> findByName(String name);

    // Search by name (partial match)
    List<Node> findByNameContainingIgnoreCase(String name);

    // Find by province
    List<Node> findByProvince(String province);

    // Find by district
    List<Node> findByDistrict(String district);

    // Find by node type
    List<Node> findByNodeType(String nodeType);

    // Custom search query
    @Query("SELECT n FROM Node n WHERE LOWER(n.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Node> searchByName(@Param("searchTerm") String searchTerm);

    // Find by province and type
    @Query("SELECT n FROM Node n WHERE n.province = :province AND n.nodeType = :type")
    List<Node> findByProvinceAndType(@Param("province") String province, @Param("type") String type);
}