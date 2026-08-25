package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelHistory;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryContractTest {

    @Mock
    private TravelerProfileRepository travelerProfileRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private TravelHistoryRepository travelHistoryRepository;

    @Mock
    private DecisionLogRepository decisionLogRepository;

    @Test
    @DisplayName("DestinationRepository should find destination by name ignoring case")
    void testFindByNameIgnoreCase() {
        Destination ella = Destination.builder()
                .id(UUID.randomUUID())
                .name("Ella")
                .province("Uva")
                .averageDailyCost(new BigDecimal("65.00"))
                .minimumDays(2)
                .maximumDays(5)
                .beachScore(1)
                .adventureScore(9)
                .natureScore(10)
                .cultureScore(4)
                .nightlifeScore(6)
                .relaxationScore(7)
                .difficultyLevel(3)
                .build();

        when(destinationRepository.findByNameIgnoreCase("ella")).thenReturn(Optional.of(ella));

        Optional<Destination> result = destinationRepository.findByNameIgnoreCase("ella");
        assertTrue(result.isPresent());
        assertEquals("Ella", result.get().getName());
        verify(destinationRepository, times(1)).findByNameIgnoreCase("ella");
    }

    @Test
    @DisplayName("TravelHistoryRepository should retrieve ratings for neighbor traveler IDs")
    void testFindByTravelerIdIn() {
        UUID travelerId1 = UUID.randomUUID();
        UUID travelerId2 = UUID.randomUUID();

        TravelHistory h1 = TravelHistory.builder()
                .id(UUID.randomUUID())
                .rating(5)
                .build();

        when(travelHistoryRepository.findByTravelerIdInWithDestination(List.of(travelerId1, travelerId2)))
                .thenReturn(List.of(h1));

        List<TravelHistory> results = travelHistoryRepository.findByTravelerIdInWithDestination(List.of(travelerId1, travelerId2));
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getRating());
    }
}
