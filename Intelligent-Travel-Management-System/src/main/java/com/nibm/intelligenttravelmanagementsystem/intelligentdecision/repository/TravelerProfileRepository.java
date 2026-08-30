package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TravelerProfileRepository extends JpaRepository<TravelerProfile, UUID> {
}
