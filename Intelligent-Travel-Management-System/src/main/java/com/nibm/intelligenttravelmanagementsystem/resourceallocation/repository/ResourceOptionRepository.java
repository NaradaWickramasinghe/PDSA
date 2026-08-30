package com.nibm.intelligenttravelmanagementsystem.resourceallocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceOptionRepository extends JpaRepository<ResourceOptionEntity, String> {
    List<ResourceOptionEntity> findByAvailableTrue();
}
