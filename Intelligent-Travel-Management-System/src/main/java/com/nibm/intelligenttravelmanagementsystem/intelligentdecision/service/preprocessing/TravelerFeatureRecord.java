package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
public class TravelerFeatureRecord {

    private UUID id;
    private double budget;
    private int durationDays;
    private int groupSize;
    private AgeGroup ageGroup;
    private TravelStyle travelStyle;

    private int beachPreference;
    private int adventurePreference;
    private int naturePreference;
    private int culturePreference;
    private int nightlifePreference;
    private int relaxationPreference;

    private String targetDestination;

    public static TravelerFeatureRecord fromEntity(TravelerProfile profile, String targetDestination) {
        return TravelerFeatureRecord.builder()
                .id(profile.getId())
                .budget(profile.getBudget() != null ? profile.getBudget().doubleValue() : 0.0)
                .durationDays(profile.getDurationDays() != null ? profile.getDurationDays() : 1)
                .groupSize(profile.getGroupSize() != null ? profile.getGroupSize() : 1)
                .ageGroup(profile.getAgeGroup() != null ? profile.getAgeGroup() : AgeGroup.YOUNG_ADULT)
                .travelStyle(profile.getTravelStyle() != null ? profile.getTravelStyle() : TravelStyle.SOLO)
                .beachPreference(profile.getBeachPreference() != null ? profile.getBeachPreference() : 3)
                .adventurePreference(profile.getAdventurePreference() != null ? profile.getAdventurePreference() : 3)
                .naturePreference(profile.getNaturePreference() != null ? profile.getNaturePreference() : 3)
                .culturePreference(profile.getCulturePreference() != null ? profile.getCulturePreference() : 3)
                .nightlifePreference(profile.getNightlifePreference() != null ? profile.getNightlifePreference() : 3)
                .relaxationPreference(profile.getRelaxationPreference() != null ? profile.getRelaxationPreference() : 3)
                .targetDestination(targetDestination)
                .build();
    }
}
