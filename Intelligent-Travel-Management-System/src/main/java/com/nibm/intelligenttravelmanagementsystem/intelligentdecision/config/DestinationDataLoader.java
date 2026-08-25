package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.config;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationDataLoader implements CommandLineRunner {

    private final DestinationRepository destinationRepository;

    @Override
    public void run(String... args) {
        if (destinationRepository.count() == 0) {
            log.info("Destination table is empty. Seeding baseline Sri Lankan destinations...");
            List<Destination> destinations = List.of(
                    Destination.builder().name("Ella").province("Uva").averageDailyCost(new BigDecimal("65.00")).minimumDays(2).maximumDays(5).beachScore(1).adventureScore(9).natureScore(10).cultureScore(4).nightlifeScore(6).relaxationScore(7).difficultyLevel(3).build(),
                    Destination.builder().name("Mirissa").province("Southern").averageDailyCost(new BigDecimal("75.00")).minimumDays(2).maximumDays(6).beachScore(10).adventureScore(6).natureScore(7).cultureScore(3).nightlifeScore(9).relaxationScore(8).difficultyLevel(2).build(),
                    Destination.builder().name("Sigiriya").province("Central").averageDailyCost(new BigDecimal("80.00")).minimumDays(1).maximumDays(3).beachScore(1).adventureScore(8).natureScore(8).cultureScore(10).nightlifeScore(2).relaxationScore(5).difficultyLevel(3).build(),
                    Destination.builder().name("Kandy").province("Central").averageDailyCost(new BigDecimal("70.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(4).natureScore(6).cultureScore(10).nightlifeScore(4).relaxationScore(7).difficultyLevel(1).build(),
                    Destination.builder().name("Nuwara Eliya").province("Central").averageDailyCost(new BigDecimal("85.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(5).natureScore(9).cultureScore(6).nightlifeScore(3).relaxationScore(9).difficultyLevel(2).build(),
                    Destination.builder().name("Arugam Bay").province("Eastern").averageDailyCost(new BigDecimal("60.00")).minimumDays(3).maximumDays(7).beachScore(10).adventureScore(9).natureScore(6).cultureScore(2).nightlifeScore(8).relaxationScore(6).difficultyLevel(3).build(),
                    Destination.builder().name("Yala National Park").province("Southern").averageDailyCost(new BigDecimal("120.00")).minimumDays(1).maximumDays(3).beachScore(2).adventureScore(8).natureScore(10).cultureScore(2).nightlifeScore(1).relaxationScore(4).difficultyLevel(2).build(),
                    Destination.builder().name("Galle Fort").province("Southern").averageDailyCost(new BigDecimal("95.00")).minimumDays(1).maximumDays(3).beachScore(7).adventureScore(3).natureScore(4).cultureScore(9).nightlifeScore(7).relaxationScore(8).difficultyLevel(1).build(),
                    Destination.builder().name("Knuckles Mountain Range").province("Central").averageDailyCost(new BigDecimal("50.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(10).natureScore(10).cultureScore(2).nightlifeScore(1).relaxationScore(5).difficultyLevel(4).build(),
                    Destination.builder().name("Bentota").province("Southern").averageDailyCost(new BigDecimal("150.00")).minimumDays(2).maximumDays(5).beachScore(9).adventureScore(4).natureScore(6).cultureScore(4).nightlifeScore(5).relaxationScore(10).difficultyLevel(1).build(),
                    Destination.builder().name("Anuradhapura").province("North Central").averageDailyCost(new BigDecimal("55.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(3).natureScore(5).cultureScore(10).nightlifeScore(1).relaxationScore(6).difficultyLevel(1).build(),
                    Destination.builder().name("Trincomalee").province("Eastern").averageDailyCost(new BigDecimal("70.00")).minimumDays(2).maximumDays(5).beachScore(9).adventureScore(6).natureScore(7).cultureScore(6).nightlifeScore(4).relaxationScore(8).difficultyLevel(2).build()
            );
            destinationRepository.saveAll(destinations);
            log.info("Successfully seeded {} destinations into repository.", destinations.size());
        }
    }
}
