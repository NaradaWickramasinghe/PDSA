package com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceOptionRepository extends JpaRepository<ResourceOptionEntity, String> {

    List<ResourceOptionEntity> findByAvailableTrue();

    @Query("SELECT r FROM ResourceOptionEntity r WHERE r.available = true AND (LOWER(r.destination) = LOWER(:destination) OR UPPER(r.destination) = 'ALL')")
    List<ResourceOptionEntity> findCandidateOptionsForDestination(@Param("destination") String destination);

    List<ResourceOptionEntity> findByAvailableTrueAndCategory(ResourceCategory category);
}
