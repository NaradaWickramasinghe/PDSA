package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.AgeGroup;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelStyle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataPreprocessor {

    // Fixed Baseline Normalization Bounds (fitted on population/domain ground-truth)
    public static final double MIN_BUDGET = 100.0;
    public static final double MAX_BUDGET = 3000.0;

    public static final double MIN_DURATION = 1.0;
    public static final double MAX_DURATION = 14.0;

    public static final double MIN_GROUP_SIZE = 1.0;
    public static final double MAX_GROUP_SIZE = 10.0;

    public static final double MIN_PREFERENCE = 1.0;
    public static final double MAX_PREFERENCE = 5.0;

    public static final int KNN_FEATURE_DIMENSION = 20; // 3 continuous + 6 preferences + 4 age OHE + 7 style OHE

    /**
     * Cleans, imputes missing values, and validates raw traveler input.
     */
    public TravelerFeatureRecord cleanAndImpute(TravelerFeatureRecord raw) {
        if (raw == null) {
            raw = TravelerFeatureRecord.builder().build();
        }

        // 1. Budget validation & missing value handling
        double budget = raw.getBudget();
        if (budget <= 0.0) {
            budget = 800.0; // Default median budget
        } else {
            budget = Math.max(MIN_BUDGET, Math.min(MAX_BUDGET, budget)); // Clamp
        }

        // 2. Duration validation & missing value handling
        int duration = raw.getDurationDays();
        if (duration <= 0) {
            duration = 4; // Default median duration
        } else {
            duration = (int) Math.max(MIN_DURATION, Math.min(MAX_DURATION, duration));
        }

        // 3. Group size validation & missing value handling
        int groupSize = raw.getGroupSize();
        if (groupSize <= 0) {
            groupSize = 2; // Default group size
        } else {
            groupSize = (int) Math.max(MIN_GROUP_SIZE, Math.min(MAX_GROUP_SIZE, groupSize));
        }

        // 4. Age Group missing value handling
        AgeGroup ageGroup = raw.getAgeGroup();
        if (ageGroup == null) {
            ageGroup = AgeGroup.YOUNG_ADULT;
        }

        // 5. Travel Style missing value handling & context inference
        TravelStyle travelStyle = raw.getTravelStyle();
        if (travelStyle == null) {
            if (groupSize == 1) {
                travelStyle = TravelStyle.SOLO;
            } else if (groupSize == 2) {
                travelStyle = TravelStyle.COUPLE;
            } else {
                travelStyle = TravelStyle.FRIENDS;
            }
        }

        // 6. Preference ratings (1 to 5) clamping & default imputation
        int beach = clampPreference(raw.getBeachPreference());
        int adventure = clampPreference(raw.getAdventurePreference());
        int nature = clampPreference(raw.getNaturePreference());
        int culture = clampPreference(raw.getCulturePreference());
        int nightlife = clampPreference(raw.getNightlifePreference());
        int relaxation = clampPreference(raw.getRelaxationPreference());

        return TravelerFeatureRecord.builder()
                .id(raw.getId())
                .budget(budget)
                .durationDays(duration)
                .groupSize(groupSize)
                .ageGroup(ageGroup)
                .travelStyle(travelStyle)
                .beachPreference(beach)
                .adventurePreference(adventure)
                .naturePreference(nature)
                .culturePreference(culture)
                .nightlifePreference(nightlife)
                .relaxationPreference(relaxation)
                .targetDestination(raw.getTargetDestination())
                .build();
    }

    private int clampPreference(int value) {
        if (value <= 0) {
            return 3; // Neutral default median
        }
        return Math.max(1, Math.min(5, value));
    }

    /**
     * Min-Max Normalization helper: X' = (X - min) / (max - min)
     */
    public double normalize(double value, double min, double max) {
        if (max == min) return 0.0;
        double clamped = Math.max(min, Math.min(max, value));
        return (clamped - min) / (max - min);
    }

    /**
     * Prepares normalized feature vector for k-NN.
     * Guaranteed to use fixed training bounds during both training and prediction.
     *
     * Vector layout (20 dimensions):
     * [0]  budget_norm
     * [1]  duration_norm
     * [2]  groupSize_norm
     * [3]  beach_norm
     * [4]  adventure_norm
     * [5]  nature_norm
     * [6]  culture_norm
     * [7]  nightlife_norm
     * [8]  relaxation_norm
     * [9..12] AgeGroup One-Hot (TEEN, YOUNG_ADULT, ADULT, SENIOR)
     * [13..19] TravelStyle One-Hot (SOLO, COUPLE, FAMILY, FRIENDS, ADVENTURE, LUXURY, BUDGET)
     */
    public double[] extractKnnFeatures(TravelerFeatureRecord record) {
        TravelerFeatureRecord cleaned = cleanAndImpute(record);
        double[] vector = new double[KNN_FEATURE_DIMENSION];

        // Scaled numeric features
        vector[0] = normalize(cleaned.getBudget(), MIN_BUDGET, MAX_BUDGET);
        vector[1] = normalize(cleaned.getDurationDays(), MIN_DURATION, MAX_DURATION);
        vector[2] = normalize(cleaned.getGroupSize(), MIN_GROUP_SIZE, MAX_GROUP_SIZE);

        // Scaled ordinal preference features
        vector[3] = normalize(cleaned.getBeachPreference(), MIN_PREFERENCE, MAX_PREFERENCE);
        vector[4] = normalize(cleaned.getAdventurePreference(), MIN_PREFERENCE, MAX_PREFERENCE);
        vector[5] = normalize(cleaned.getNaturePreference(), MIN_PREFERENCE, MAX_PREFERENCE);
        vector[6] = normalize(cleaned.getCulturePreference(), MIN_PREFERENCE, MAX_PREFERENCE);
        vector[7] = normalize(cleaned.getNightlifePreference(), MIN_PREFERENCE, MAX_PREFERENCE);
        vector[8] = normalize(cleaned.getRelaxationPreference(), MIN_PREFERENCE, MAX_PREFERENCE);

        // Age Group One-Hot Encoding
        AgeGroup age = cleaned.getAgeGroup();
        vector[9] = (age == AgeGroup.TEEN) ? 1.0 : 0.0;
        vector[10] = (age == AgeGroup.YOUNG_ADULT) ? 1.0 : 0.0;
        vector[11] = (age == AgeGroup.ADULT) ? 1.0 : 0.0;
        vector[12] = (age == AgeGroup.SENIOR) ? 1.0 : 0.0;

        // Travel Style One-Hot Encoding
        TravelStyle style = cleaned.getTravelStyle();
        vector[13] = (style == TravelStyle.SOLO) ? 1.0 : 0.0;
        vector[14] = (style == TravelStyle.COUPLE) ? 1.0 : 0.0;
        vector[15] = (style == TravelStyle.FAMILY) ? 1.0 : 0.0;
        vector[16] = (style == TravelStyle.FRIENDS) ? 1.0 : 0.0;
        vector[17] = (style == TravelStyle.ADVENTURE) ? 1.0 : 0.0;
        vector[18] = (style == TravelStyle.LUXURY) ? 1.0 : 0.0;
        vector[19] = (style == TravelStyle.BUDGET) ? 1.0 : 0.0;

        return vector;
    }

    /**
     * Constructs a synthetic balanced dataset of 600 records grounded in Sri Lankan travel personas.
     */
    public List<TravelerFeatureRecord> generateSyntheticDataset(long seed) {
        Random rng = new Random(seed);
        List<TravelerFeatureRecord> dataset = new ArrayList<>(600);

        String[] destinations = {
                "Ella", "Mirissa", "Sigiriya", "Kandy", "Nuwara Eliya", "Arugam Bay",
                "Yala National Park", "Galle Fort", "Knuckles Mountain Range", "Bentota",
                "Anuradhapura", "Trincomalee"
        };

        // 50 samples per destination = 600 total
        for (String dest : destinations) {
            for (int i = 0; i < 50; i++) {
                dataset.add(createSampleForDestination(dest, rng));
            }
        }
        return dataset;
    }

    private TravelerFeatureRecord createSampleForDestination(String destination, Random rng) {
        double budget;
        int duration;
        int groupSize;
        AgeGroup ageGroup;
        TravelStyle travelStyle;
        int beach, adv, nature, culture, nightlife, relax;

        switch (destination) {
            case "Ella" -> {
                budget = 400 + rng.nextDouble() * 500;
                duration = 3 + rng.nextInt(4);
                groupSize = 1 + rng.nextInt(3);
                ageGroup = rng.nextBoolean() ? AgeGroup.YOUNG_ADULT : AgeGroup.ADULT;
                travelStyle = TravelStyle.ADVENTURE;
                beach = 1 + rng.nextInt(2);
                adv = 4 + rng.nextInt(2);
                nature = 4 + rng.nextInt(2);
                culture = 2 + rng.nextInt(3);
                nightlife = 2 + rng.nextInt(3);
                relax = 3 + rng.nextInt(3);
            }
            case "Knuckles Mountain Range" -> {
                budget = 300 + rng.nextDouble() * 400;
                duration = 2 + rng.nextInt(3);
                groupSize = 1 + rng.nextInt(4);
                ageGroup = AgeGroup.YOUNG_ADULT;
                travelStyle = TravelStyle.ADVENTURE;
                beach = 1;
                adv = 5;
                nature = 5;
                culture = 1 + rng.nextInt(2);
                nightlife = 1;
                relax = 2 + rng.nextInt(2);
            }
            case "Mirissa" -> {
                budget = 600 + rng.nextDouble() * 600;
                duration = 3 + rng.nextInt(4);
                groupSize = 2 + rng.nextInt(4);
                ageGroup = AgeGroup.YOUNG_ADULT;
                travelStyle = TravelStyle.FRIENDS;
                beach = 5;
                adv = 3 + rng.nextInt(2);
                nature = 3 + rng.nextInt(2);
                culture = 1 + rng.nextInt(2);
                nightlife = 4 + rng.nextInt(2);
                relax = 4 + rng.nextInt(2);
            }
            case "Arugam Bay" -> {
                budget = 500 + rng.nextDouble() * 600;
                duration = 4 + rng.nextInt(5);
                groupSize = 2 + rng.nextInt(3);
                ageGroup = AgeGroup.YOUNG_ADULT;
                travelStyle = TravelStyle.ADVENTURE;
                beach = 5;
                adv = 5;
                nature = 3 + rng.nextInt(2);
                culture = 1 + rng.nextInt(2);
                nightlife = 4 + rng.nextInt(2);
                relax = 3 + rng.nextInt(3);
            }
            case "Sigiriya" -> {
                budget = 600 + rng.nextDouble() * 700;
                duration = 2 + rng.nextInt(3);
                groupSize = 2 + rng.nextInt(3);
                ageGroup = AgeGroup.ADULT;
                travelStyle = TravelStyle.COUPLE;
                beach = 1;
                adv = 4 + rng.nextInt(2);
                nature = 4 + rng.nextInt(2);
                culture = 5;
                nightlife = 1 + rng.nextInt(2);
                relax = 2 + rng.nextInt(3);
            }
            case "Kandy" -> {
                budget = 500 + rng.nextDouble() * 500;
                duration = 2 + rng.nextInt(3);
                groupSize = 2 + rng.nextInt(4);
                ageGroup = rng.nextBoolean() ? AgeGroup.ADULT : AgeGroup.SENIOR;
                travelStyle = TravelStyle.FAMILY;
                beach = 1;
                adv = 2 + rng.nextInt(2);
                nature = 3 + rng.nextInt(3);
                culture = 5;
                nightlife = 1 + rng.nextInt(2);
                relax = 3 + rng.nextInt(3);
            }
            case "Anuradhapura" -> {
                budget = 400 + rng.nextDouble() * 500;
                duration = 2 + rng.nextInt(3);
                groupSize = 2 + rng.nextInt(3);
                ageGroup = AgeGroup.SENIOR;
                travelStyle = TravelStyle.COUPLE;
                beach = 1;
                adv = 1 + rng.nextInt(2);
                nature = 3 + rng.nextInt(2);
                culture = 5;
                nightlife = 1;
                relax = 4 + rng.nextInt(2);
            }
            case "Bentota" -> {
                budget = 1200 + rng.nextDouble() * 1200;
                duration = 3 + rng.nextInt(5);
                groupSize = 2 + rng.nextInt(3);
                ageGroup = AgeGroup.ADULT;
                travelStyle = TravelStyle.LUXURY;
                beach = 5;
                adv = 2 + rng.nextInt(2);
                nature = 3 + rng.nextInt(2);
                culture = 2 + rng.nextInt(2);
                nightlife = 2 + rng.nextInt(3);
                relax = 5;
            }
            case "Nuwara Eliya" -> {
                budget = 800 + rng.nextDouble() * 800;
                duration = 2 + rng.nextInt(4);
                groupSize = 2 + rng.nextInt(4);
                ageGroup = AgeGroup.ADULT;
                travelStyle = TravelStyle.FAMILY;
                beach = 1;
                adv = 2 + rng.nextInt(3);
                nature = 5;
                culture = 3 + rng.nextInt(2);
                nightlife = 1 + rng.nextInt(2);
                relax = 5;
            }
            case "Yala National Park" -> {
                budget = 900 + rng.nextDouble() * 900;
                duration = 2 + rng.nextInt(3);
                groupSize = 2 + rng.nextInt(4);
                ageGroup = AgeGroup.ADULT;
                travelStyle = TravelStyle.ADVENTURE;
                beach = 2 + rng.nextInt(2);
                adv = 4 + rng.nextInt(2);
                nature = 5;
                culture = 1 + rng.nextInt(2);
                nightlife = 1;
                relax = 2 + rng.nextInt(3);
            }
            case "Galle Fort" -> {
                budget = 700 + rng.nextDouble() * 800;
                duration = 2 + rng.nextInt(3);
                groupSize = 1 + rng.nextInt(3);
                ageGroup = AgeGroup.ADULT;
                travelStyle = TravelStyle.COUPLE;
                beach = 4 + rng.nextInt(2);
                adv = 1 + rng.nextInt(2);
                nature = 2 + rng.nextInt(2);
                culture = 5;
                nightlife = 3 + rng.nextInt(3);
                relax = 4 + rng.nextInt(2);
            }
            default -> { // Trincomalee
                budget = 600 + rng.nextDouble() * 600;
                duration = 3 + rng.nextInt(4);
                groupSize = 2 + rng.nextInt(4);
                ageGroup = AgeGroup.YOUNG_ADULT;
                travelStyle = TravelStyle.FRIENDS;
                beach = 5;
                adv = 3 + rng.nextInt(2);
                nature = 4 + rng.nextInt(2);
                culture = 3 + rng.nextInt(2);
                nightlife = 2 + rng.nextInt(3);
                relax = 4 + rng.nextInt(2);
            }
        }

        return TravelerFeatureRecord.builder()
                .budget(budget)
                .durationDays(duration)
                .groupSize(groupSize)
                .ageGroup(ageGroup)
                .travelStyle(travelStyle)
                .beachPreference(beach)
                .adventurePreference(adv)
                .naturePreference(nature)
                .culturePreference(culture)
                .nightlifePreference(nightlife)
                .relaxationPreference(relax)
                .targetDestination(destination)
                .build();
    }
}
