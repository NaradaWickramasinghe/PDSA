package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.evaluation;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.Destination;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.DestinationRatingRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.HistoricalTravelerIndexItem;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.knn.KnnRecommendationResult;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreeClassifier;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.DataPreprocessor;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankedDestinationCandidate;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking.RankingWeights;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.RankingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Module4EvaluationRunnerTest {

    private static final String[] FEATURE_NAMES = {
            "Budget", "DurationDays", "GroupSize",
            "BeachPref", "AdventurePref", "NaturePref", "CulturePref", "NightlifePref", "RelaxationPref",
            "IsTeen", "IsYoungAdult", "IsAdult", "IsSenior",
            "IsSolo", "IsCouple", "IsFamily", "IsFriends", "IsAdventure", "IsLuxury", "IsBudget"
    };

    @Test
    @DisplayName("Execute full experimental evaluation on held-out test data (N=180)")
    public void runFullExperiment() {
        DataPreprocessor preprocessor = new DataPreprocessor();
        List<TravelerFeatureRecord> fullDataset = preprocessor.generateSyntheticDataset(42L);

        // 70% Train (420 samples), 30% Test (180 samples)
        Collections.shuffle(fullDataset, new Random(42));
        int trainSize = (int) (fullDataset.size() * 0.70); // 420
        List<TravelerFeatureRecord> trainSet = fullDataset.subList(0, trainSize);
        List<TravelerFeatureRecord> testSet = fullDataset.subList(trainSize, fullDataset.size());

        List<Destination> candidateDestinations = createTestDestinations();

        System.out.println("=========================================================================================");
        System.out.println(" EXPERIMENTAL EVALUATION REPORT: MODULE 4 INTELLIGENT DECISION SUBSYSTEM");
        System.out.println(" Dataset: 600 Samples | Train: 420 (70%) | Test: 180 (30%) | Seed: 42");
        System.out.println("=========================================================================================");
        System.out.printf("%-18s | %-32s | %-8s | %-9s | %-8s | %-8s%n", "Model", "Configuration", "Accuracy", "Precision", "Recall", "F1");
        System.out.println("-----------------------------------------------------------------------------------------");

        // -------------------------------------------------------------
        // 1. DECISION TREE EVALUATIONS (CART)
        // -------------------------------------------------------------
        int[] treeDepths = {3, 5, 7, 10};
        int[] minSamples = {2, 5};

        double[][] trainX = new double[trainSet.size()][DataPreprocessor.KNN_FEATURE_DIMENSION];
        String[] trainY = new String[trainSet.size()];
        for (int i = 0; i < trainSet.size(); i++) {
            trainX[i] = preprocessor.extractKnnFeatures(trainSet.get(i));
            trainY[i] = trainSet.get(i).getTargetDestination();
        }

        for (int depth : treeDepths) {
            for (int minS : minSamples) {
                DecisionTreeClassifier dt = new DecisionTreeClassifier(depth, minS, FEATURE_NAMES);
                dt.train(trainX, trainY);

                List<String> actuals = new ArrayList<>();
                List<String> preds = new ArrayList<>();

                for (TravelerFeatureRecord testSample : testSet) {
                    actuals.add(testSample.getTargetDestination());
                    double[] testVec = preprocessor.extractKnnFeatures(testSample);
                    DecisionTreePrediction pred = dt.predict(testVec);
                    preds.add(pred.getPredictedClass());
                }

                EvaluationMetrics metrics = calculateMetrics(actuals, preds);
                System.out.printf("%-18s | MaxDepth=%-2d, MinSamples=%-2d          | %-8.4f | %-9.4f | %-8.4f | %-8.4f%n",
                        "Decision Tree", depth, minS, metrics.accuracy, metrics.precision, metrics.recall, metrics.f1);
            }
        }

        System.out.println("-----------------------------------------------------------------------------------------");

        // -------------------------------------------------------------
        // 2. KNN EVALUATIONS
        // -------------------------------------------------------------
        List<HistoricalTravelerIndexItem> knnIndex = buildKnnIndex(trainSet, preprocessor);

        int[] kValues = {1, 3, 5, 7, 9};
        for (int k : kValues) {
            List<String> actuals = new ArrayList<>();
            List<String> preds = new ArrayList<>();

            for (TravelerFeatureRecord testSample : testSet) {
                actuals.add(testSample.getTargetDestination());
                String topPred = predictKnnTop(testSample, knnIndex, preprocessor, k);
                preds.add(topPred);
            }

            EvaluationMetrics metrics = calculateMetrics(actuals, preds);
            System.out.printf("%-18s | K=%-2d (Inverse-Distance Weighted)       | %-8.4f | %-9.4f | %-8.4f | %-8.4f%n",
                    "KNN", k, metrics.accuracy, metrics.precision, metrics.recall, metrics.f1);
        }

        System.out.println("-----------------------------------------------------------------------------------------");

        // -------------------------------------------------------------
        // 3. HYBRID (DT + KNN + RANKING) EVALUATIONS
        // -------------------------------------------------------------
        DecisionTreeClassifier bestDt = new DecisionTreeClassifier(7, 2, FEATURE_NAMES);
        bestDt.train(trainX, trainY);

        List<WeightConfigTest> hybridConfigs = List.of(
                new WeightConfigTest("Balanced (30% DT, 25% KNN, 25% Pref, 10% Budg, 10% Dur)",
                        RankingWeights.builder().tree(0.30).knn(0.25).preference(0.25).budget(0.10).duration(0.10).build()),
                new WeightConfigTest("DT-Heavy (50% DT, 15% KNN, 15% Pref, 10% Budg, 10% Dur)",
                        RankingWeights.builder().tree(0.50).knn(0.15).preference(0.15).budget(0.10).duration(0.10).build()),
                new WeightConfigTest("KNN-Heavy (15% DT, 50% KNN, 15% Pref, 10% Budg, 10% Dur)",
                        RankingWeights.builder().tree(0.15).knn(0.50).preference(0.15).budget(0.10).duration(0.10).build()),
                new WeightConfigTest("Preference-Heavy (15% DT, 15% KNN, 50% Pref, 10% Budg, 10% Dur)",
                        RankingWeights.builder().tree(0.15).knn(0.15).preference(0.50).budget(0.10).duration(0.10).build())
        );

        RankingServiceImpl rankingService = new RankingServiceImpl(
                RankingWeights.builder().tree(0.30).knn(0.25).preference(0.25).budget(0.10).duration(0.10).build(),
                preprocessor
        );

        for (WeightConfigTest hConfig : hybridConfigs) {
            List<String> actuals = new ArrayList<>();
            List<String> preds = new ArrayList<>();

            for (TravelerFeatureRecord testSample : testSet) {
                actuals.add(testSample.getTargetDestination());

                double[] testVec = preprocessor.extractKnnFeatures(testSample);
                DecisionTreePrediction dtPred = bestDt.predict(testVec);
                KnnRecommendationResult knnRes = computeKnnResult(testSample, knnIndex, preprocessor, 5);

                List<RankedDestinationCandidate> ranked = rankingService.rankDestinations(
                        testSample,
                        candidateDestinations,
                        dtPred,
                        knnRes,
                        1,
                        hConfig.weights
                );

                String topRanked = !ranked.isEmpty() ? ranked.get(0).getDestinationName() : "Ella";
                preds.add(topRanked);
            }

            EvaluationMetrics metrics = calculateMetrics(actuals, preds);
            System.out.printf("%-18s | %-32s | %-8.4f | %-9.4f | %-8.4f | %-8.4f%n",
                    "Hybrid Ensemble", hConfig.name.substring(0, Math.min(32, hConfig.name.length())),
                    metrics.accuracy, metrics.precision, metrics.recall, metrics.f1);
        }

        System.out.println("=========================================================================================");
    }

    private static class WeightConfigTest {
        String name;
        RankingWeights weights;
        WeightConfigTest(String name, RankingWeights weights) {
            this.name = name;
            this.weights = weights;
        }
    }

    private List<HistoricalTravelerIndexItem> buildKnnIndex(List<TravelerFeatureRecord> records, DataPreprocessor preprocessor) {
        List<HistoricalTravelerIndexItem> index = new ArrayList<>();
        for (TravelerFeatureRecord r : records) {
            double[] vec = preprocessor.extractKnnFeatures(r);
            List<DestinationRatingRecord> ratings = List.of(
                    DestinationRatingRecord.builder()
                            .destinationName(r.getTargetDestination())
                            .rating(5)
                            .build()
            );
            index.add(HistoricalTravelerIndexItem.builder()
                    .travelerId(UUID.randomUUID())
                    .featureVector(vec)
                    .visitedDestinations(ratings)
                    .build());
        }
        return index;
    }

    private String predictKnnTop(TravelerFeatureRecord sample, List<HistoricalTravelerIndexItem> index, DataPreprocessor preprocessor, int k) {
        KnnRecommendationResult res = computeKnnResult(sample, index, preprocessor, k);
        if (res.getTopEvidences().isEmpty()) return "Ella";
        return res.getTopEvidences().get(0).getDestinationName();
    }

    private KnnRecommendationResult computeKnnResult(TravelerFeatureRecord sample, List<HistoricalTravelerIndexItem> index, DataPreprocessor preprocessor, int k) {
        double[] targetVec = preprocessor.extractKnnFeatures(sample);
        List<NeighborItem> neighbors = new ArrayList<>();
        for (HistoricalTravelerIndexItem item : index) {
            double dist = euclideanDistance(targetVec, item.getFeatureVector());
            neighbors.add(new NeighborItem(item, dist));
        }
        neighbors.sort(Comparator.comparingDouble(n -> n.dist));
        List<NeighborItem> topK = neighbors.stream().limit(k).collect(Collectors.toList());

        Map<String, Double> voteMap = new HashMap<>();
        for (NeighborItem n : topK) {
            double weight = 1.0 / (n.dist + 1e-4);
            for (DestinationRatingRecord dr : n.item.getVisitedDestinations()) {
                voteMap.merge(dr.getDestinationName(), weight * (dr.getRating() / 5.0), Double::sum);
            }
        }

        List<KnnRecommendationResult.KnnDestinationEvidence> evidences = new ArrayList<>();
        double maxVote = voteMap.values().stream().max(Double::compareTo).orElse(1.0);
        for (Map.Entry<String, Double> e : voteMap.entrySet()) {
            evidences.add(KnnRecommendationResult.KnnDestinationEvidence.builder()
                    .destinationName(e.getKey())
                    .evidenceScore(e.getValue() / maxVote)
                    .voteCount(1)
                    .averageRating(5.0)
                    .explanation("Voted by nearest neighbors")
                    .build());
        }
        evidences.sort(Comparator.comparingDouble(KnnRecommendationResult.KnnDestinationEvidence::getEvidenceScore).reversed());

        return KnnRecommendationResult.builder()
                .kUsed(k)
                .destinationScores(voteMap)
                .topEvidences(evidences)
                .build();
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    private static class NeighborItem {
        HistoricalTravelerIndexItem item;
        double dist;
        NeighborItem(HistoricalTravelerIndexItem item, double dist) {
            this.item = item;
            this.dist = dist;
        }
    }

    private static class EvaluationMetrics {
        double accuracy;
        double precision;
        double recall;
        double f1;
    }

    private EvaluationMetrics calculateMetrics(List<String> actuals, List<String> preds) {
        int n = actuals.size();
        int correct = 0;
        Set<String> classes = new HashSet<>(actuals);

        Map<String, Integer> tp = new HashMap<>();
        Map<String, Integer> fp = new HashMap<>();
        Map<String, Integer> fn = new HashMap<>();

        for (String c : classes) {
            tp.put(c, 0);
            fp.put(c, 0);
            fn.put(c, 0);
        }

        for (int i = 0; i < n; i++) {
            String act = actuals.get(i);
            String prd = preds.get(i);

            if (act.equalsIgnoreCase(prd)) {
                correct++;
                tp.put(act, tp.get(act) + 1);
            } else {
                fp.put(prd, fp.getOrDefault(prd, 0) + 1);
                fn.put(act, fn.getOrDefault(act, 0) + 1);
            }
        }

        double accuracy = (double) correct / n;

        double sumPrec = 0.0;
        double sumRec = 0.0;
        int activeClasses = 0;

        for (String c : classes) {
            int t = tp.getOrDefault(c, 0);
            int f_p = fp.getOrDefault(c, 0);
            int f_n = fn.getOrDefault(c, 0);

            double p = (t + f_p > 0) ? (double) t / (t + f_p) : 0.0;
            double r = (t + f_n > 0) ? (double) t / (t + f_n) : 0.0;

            sumPrec += p;
            sumRec += r;
            activeClasses++;
        }

        double macroPrec = activeClasses > 0 ? sumPrec / activeClasses : 0.0;
        double macroRec = activeClasses > 0 ? sumRec / activeClasses : 0.0;
        double macroF1 = (macroPrec + macroRec > 0) ? (2.0 * macroPrec * macroRec) / (macroPrec + macroRec) : 0.0;

        EvaluationMetrics m = new EvaluationMetrics();
        m.accuracy = accuracy;
        m.precision = macroPrec;
        m.recall = macroRec;
        m.f1 = macroF1;
        return m;
    }

    private List<Destination> createTestDestinations() {
        return List.of(
                Destination.builder().name("Ella").averageDailyCost(new BigDecimal("65.00")).minimumDays(2).maximumDays(5).beachScore(1).adventureScore(9).natureScore(10).cultureScore(4).nightlifeScore(6).relaxationScore(7).build(),
                Destination.builder().name("Mirissa").averageDailyCost(new BigDecimal("75.00")).minimumDays(2).maximumDays(6).beachScore(10).adventureScore(6).natureScore(7).cultureScore(3).nightlifeScore(9).relaxationScore(8).build(),
                Destination.builder().name("Sigiriya").averageDailyCost(new BigDecimal("80.00")).minimumDays(1).maximumDays(3).beachScore(1).adventureScore(8).natureScore(8).cultureScore(10).nightlifeScore(2).relaxationScore(5).build(),
                Destination.builder().name("Kandy").averageDailyCost(new BigDecimal("70.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(4).natureScore(6).cultureScore(10).nightlifeScore(4).relaxationScore(7).build(),
                Destination.builder().name("Nuwara Eliya").averageDailyCost(new BigDecimal("85.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(5).natureScore(9).cultureScore(6).nightlifeScore(3).relaxationScore(9).build(),
                Destination.builder().name("Arugam Bay").averageDailyCost(new BigDecimal("60.00")).minimumDays(3).maximumDays(7).beachScore(10).adventureScore(9).natureScore(6).cultureScore(2).nightlifeScore(8).relaxationScore(6).build(),
                Destination.builder().name("Yala National Park").averageDailyCost(new BigDecimal("120.00")).minimumDays(1).maximumDays(3).beachScore(2).adventureScore(8).natureScore(10).cultureScore(2).nightlifeScore(1).relaxationScore(4).build(),
                Destination.builder().name("Galle Fort").averageDailyCost(new BigDecimal("95.00")).minimumDays(1).maximumDays(3).beachScore(7).adventureScore(3).natureScore(4).cultureScore(9).nightlifeScore(7).relaxationScore(8).build(),
                Destination.builder().name("Knuckles Mountain Range").averageDailyCost(new BigDecimal("50.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(10).natureScore(10).cultureScore(2).nightlifeScore(1).relaxationScore(5).build(),
                Destination.builder().name("Bentota").averageDailyCost(new BigDecimal("150.00")).minimumDays(2).maximumDays(5).beachScore(9).adventureScore(4).natureScore(6).cultureScore(4).nightlifeScore(5).relaxationScore(10).build(),
                Destination.builder().name("Anuradhapura").averageDailyCost(new BigDecimal("55.00")).minimumDays(2).maximumDays(4).beachScore(1).adventureScore(3).natureScore(5).cultureScore(10).nightlifeScore(1).relaxationScore(6).build(),
                Destination.builder().name("Trincomalee").averageDailyCost(new BigDecimal("70.00")).minimumDays(2).maximumDays(5).beachScore(9).adventureScore(6).natureScore(7).cultureScore(6).nightlifeScore(4).relaxationScore(8).build()
        );
    }
}
