package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreeClassifier;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionTreeServiceImpl implements DecisionTreeService {

    private final DataPreprocessor preprocessor;

    private DecisionTreeClassifier classifier;
    private volatile boolean modelReady = false;

    private static final String[] FEATURE_NAMES = {
            "Budget", "DurationDays", "GroupSize",
            "BeachPref", "AdventurePref", "NaturePref", "CulturePref", "NightlifePref", "RelaxationPref",
            "IsTeen", "IsYoungAdult", "IsAdult", "IsSenior",
            "IsSolo", "IsCouple", "IsFamily", "IsFriends", "IsAdventure", "IsLuxury", "IsBudget"
    };

    @PostConstruct
    public void init() {
        log.info("Initializing DecisionTreeService: Loading training dataset and training model...");
        List<TravelerFeatureRecord> trainingData = preprocessor.generateSyntheticDataset(42L);
        trainModel(trainingData);
    }

    @Override
    public synchronized void trainModel(List<TravelerFeatureRecord> dataset) {
        if (dataset == null || dataset.isEmpty()) {
            throw new IllegalArgumentException("Cannot train Decision Tree on empty dataset");
        }

        int numSamples = dataset.size();
        double[][] X = new double[numSamples][DataPreprocessor.KNN_FEATURE_DIMENSION];
        String[] y = new String[numSamples];

        for (int i = 0; i < numSamples; i++) {
            TravelerFeatureRecord record = dataset.get(i);
            X[i] = preprocessor.extractKnnFeatures(record);
            y[i] = record.getTargetDestination() != null ? record.getTargetDestination() : "Ella";
        }

        // Train CART model with controlled max depth (6) and minimum leaf sample size (5) to avoid overfitting
        this.classifier = new DecisionTreeClassifier(6, 5, FEATURE_NAMES);
        this.classifier.train(X, y);
        this.modelReady = true;
        log.info("DecisionTreeService trained successfully with {} samples.", numSamples);
    }

    @Override
    public DecisionTreePrediction predict(TravelerFeatureRecord record) {
        if (!modelReady || classifier == null) {
            throw new IllegalStateException("DecisionTree model is not initialized");
        }

        double[] sampleVector = preprocessor.extractKnnFeatures(record);
        DecisionTreePrediction rawPrediction = classifier.predict(sampleVector);

        // Derive high-level suitability label based on confidence score
        SuitabilityLabel suitabilityLabel;
        double conf = rawPrediction.getConfidenceScore();
        if (conf >= 0.70) {
            suitabilityLabel = SuitabilityLabel.EXCELLENT_FIT;
        } else if (conf >= 0.45) {
            suitabilityLabel = SuitabilityLabel.MODERATE_FIT;
        } else if (conf >= 0.20) {
            suitabilityLabel = SuitabilityLabel.CHALLENGING_FIT;
        } else {
            suitabilityLabel = SuitabilityLabel.NOT_SUITABLE;
        }

        return DecisionTreePrediction.builder()
                .predictedClass(rawPrediction.getPredictedClass())
                .confidenceScore(rawPrediction.getConfidenceScore())
                .suitabilityLabel(suitabilityLabel)
                .classProbabilities(rawPrediction.getClassProbabilities())
                .decisionPathRules(rawPrediction.getDecisionPathRules())
                .rationale(String.format("Decision Tree classified '%s' with %.1f%% confidence based on rule path: %s",
                        rawPrediction.getPredictedClass(),
                        conf * 100,
                        String.join(" -> ", rawPrediction.getDecisionPathRules())))
                .build();
    }

    @Override
    public DecisionTreePrediction predict(TravelerProfile profile) {
        if (profile == null) {
            return predict(TravelerFeatureRecord.builder().build());
        }
        TravelerFeatureRecord record = TravelerFeatureRecord.fromEntity(profile, null);
        return predict(record);
    }

    @Override
    public boolean isModelReady() {
        return modelReady;
    }
}
