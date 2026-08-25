package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TravelHistoryRepository extends JpaRepository<TravelHistory, UUID> {

    List<TravelHistory> findByTravelerId(UUID travelerId);

    List<TravelHistory> findByDestinationId(UUID destinationId);

    List<TravelHistory> findByTravelerIdIn(Collection<UUID> travelerIds);

    @Query("SELECT th FROM TravelHistory th JOIN FETCH th.traveler JOIN FETCH th.destination")
    List<TravelHistory> findAllWithTravelerAndDestination();

    @Query("SELECT th FROM TravelHistory th JOIN FETCH th.destination WHERE th.traveler.id IN :travelerIds")
    List<TravelHistory> findByTravelerIdInWithDestination(@Param("travelerIds") Collection<UUID> travelerIds);
}
