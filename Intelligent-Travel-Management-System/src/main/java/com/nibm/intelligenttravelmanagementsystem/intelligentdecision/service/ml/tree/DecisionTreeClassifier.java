package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionTreeClassifier {

    private final int maxDepth;
    private final int minSamplesSplit;
    private final String[] featureNames;
    private DecisionTreeNode root;

    public DecisionTreeClassifier(int maxDepth, int minSamplesSplit, String[] featureNames) {
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.featureNames = featureNames;
    }

    public void train(double[][] X, String[] y) {
        if (X == null || y == null || X.length == 0 || X.length != y.length) {
            throw new IllegalArgumentException("Invalid training matrices provided");
        }
        int[] sampleIndices = new int[X.length];
        for (int i = 0; i < X.length; i++) {
            sampleIndices[i] = i;
        }
        this.root = buildTree(X, y, sampleIndices, 0);
    }

    private DecisionTreeNode buildTree(double[][] X, String[] y, int[] sampleIndices, int depth) {
        int numSamples = sampleIndices.length;
        Map<String, Integer> classCounts = countClasses(y, sampleIndices);
        String majorityClass = getMajorityClass(classCounts);
        double majorityConfidence = (double) classCounts.get(majorityClass) / numSamples;
        double impurity = calculateGini(classCounts, numSamples);

        // Stopping criteria: max depth reached, min samples reached, or pure node (impurity == 0)
        if (depth >= maxDepth || numSamples < minSamplesSplit || impurity <= 1e-6 || classCounts.size() <= 1) {
            return createLeafNode(majorityClass, majorityConfidence, classCounts, numSamples, impurity);
        }

        // Find optimal split across features and thresholds
        BestSplit bestSplit = findBestSplit(X, y, sampleIndices);

        if (bestSplit == null || bestSplit.leftIndices.length == 0 || bestSplit.rightIndices.length == 0) {
            return createLeafNode(majorityClass, majorityConfidence, classCounts, numSamples, impurity);
        }

        DecisionTreeNode node = DecisionTreeNode.builder()
                .isLeaf(false)
                .featureIndex(bestSplit.featureIndex)
                .threshold(bestSplit.threshold)
                .featureName(featureNames != null && bestSplit.featureIndex < featureNames.length
                        ? featureNames[bestSplit.featureIndex]
                        : "Feature_" + bestSplit.featureIndex)
                .sampleCount(numSamples)
                .impurity(impurity)
                .predictedClass(majorityClass)
                .confidence(majorityConfidence)
                .classDistribution(normalizeDistribution(classCounts, numSamples))
                .build();

        node.setLeftChild(buildTree(X, y, bestSplit.leftIndices, depth + 1));
        node.setRightChild(buildTree(X, y, bestSplit.rightIndices, depth + 1));

        return node;
    }

    private BestSplit findBestSplit(double[][] X, String[] y, int[] sampleIndices) {
        int numFeatures = X[0].length;
        int numSamples = sampleIndices.length;
        double currentGini = calculateGini(countClasses(y, sampleIndices), numSamples);

        double bestGain = 0.0;
        BestSplit bestSplit = null;

        for (int featureIdx = 0; featureIdx < numFeatures; featureIdx++) {
            // Collect unique values for this feature to test potential thresholds
            double[] featureValues = new double[numSamples];
            for (int i = 0; i < numSamples; i++) {
                featureValues[i] = X[sampleIndices[i]][featureIdx];
            }
            Arrays.sort(featureValues);

            for (int i = 0; i < numSamples - 1; i++) {
                if (Math.abs(featureValues[i] - featureValues[i + 1]) < 1e-6) {
                    continue;
                }
                double threshold = (featureValues[i] + featureValues[i + 1]) / 2.0;

                List<Integer> left = new ArrayList<>();
                List<Integer> right = new ArrayList<>();

                for (int sampleIdx : sampleIndices) {
                    if (X[sampleIdx][featureIdx] <= threshold) {
                        left.add(sampleIdx);
                    } else {
                        right.add(sampleIdx);
                    }
                }

                if (left.isEmpty() || right.isEmpty()) continue;

                // Convert the left and right index lists into simple arrays
                int[] leftArr = new int[left.size()];
                for (int j = 0; j < left.size(); j++) {
                    leftArr[j] = left.get(j);
                }

                int[] rightArr = new int[right.size()];
                for (int j = 0; j < right.size(); j++) {
                    rightArr[j] = right.get(j);
                }

                double leftGini = calculateGini(countClasses(y, leftArr), leftArr.length);
                double rightGini = calculateGini(countClasses(y, rightArr), rightArr.length);

                double weightedGini = ((double) leftArr.length / numSamples) * leftGini
                        + ((double) rightArr.length / numSamples) * rightGini;
                double gain = currentGini - weightedGini;

                if (gain > bestGain) {
                    bestGain = gain;
                    bestSplit = new BestSplit(featureIdx, threshold, leftArr, rightArr);
                }
            }
        }
        return bestSplit;
    }

    private DecisionTreeNode createLeafNode(String majorityClass, double confidence, Map<String, Integer> classCounts, int numSamples, double impurity) {
        return DecisionTreeNode.builder()
                .isLeaf(true)
                .predictedClass(majorityClass)
                .confidence(confidence)
                .classDistribution(normalizeDistribution(classCounts, numSamples))
                .sampleCount(numSamples)
                .impurity(impurity)
                .build();
    }

    private double calculateGini(Map<String, Integer> classCounts, int total) {
        if (total == 0) return 0.0;
        double sumSquares = 0.0;
        for (int count : classCounts.values()) {
            double p = (double) count / total;
            sumSquares += p * p;
        }
        return 1.0 - sumSquares;
    }

    private Map<String, Integer> countClasses(String[] y, int[] indices) {
        Map<String, Integer> counts = new HashMap<>();
        for (int idx : indices) {
            counts.put(y[idx], counts.getOrDefault(y[idx], 0) + 1);
        }
        return counts;
    }

    /**
     * Find the class label that appears the most times in the counts map.
     * This is the "majority class" — the label that the leaf node will predict.
     */
    private String getMajorityClass(Map<String, Integer> counts) {
        String bestClass = "UNKNOWN";
        int highestCount = -1;

        // Loop through each class label and its count
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > highestCount) {
                highestCount = entry.getValue();
                bestClass = entry.getKey();
            }
        }

        return bestClass;
    }

    private Map<String, Double> normalizeDistribution(Map<String, Integer> counts, int total) {
        Map<String, Double> dist = new HashMap<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            dist.put(e.getKey(), (double) e.getValue() / total);
        }
        return dist;
    }

    public DecisionTreePrediction predict(double[] sample) {
        if (root == null) {
            throw new IllegalStateException("Decision Tree has not been trained");
        }

        DecisionTreeNode current = root;
        List<String> pathRules = new ArrayList<>();

        while (!current.isLeaf()) {
            int featIdx = current.getFeatureIndex();
            double val = sample[featIdx];
            String featName = current.getFeatureName();

            if (val <= current.getThreshold()) {
                pathRules.add(String.format("%s (%.2f) <= %.2f", featName, val, current.getThreshold()));
                if (current.getLeftChild() == null) break;
                current = current.getLeftChild();
            } else {
                pathRules.add(String.format("%s (%.2f) > %.2f", featName, val, current.getThreshold()));
                if (current.getRightChild() == null) break;
                current = current.getRightChild();
            }
        }

        return DecisionTreePrediction.builder()
                .predictedClass(current.getPredictedClass())
                .confidenceScore(current.getConfidence())
                .classProbabilities(current.getClassDistribution())
                .decisionPathRules(pathRules)
                .rationale("Tree decision traversed: " + String.join(" -> ", pathRules))
                .build();
    }

    private static class BestSplit {
        final int featureIndex;
        final double threshold;
        final int[] leftIndices;
        final int[] rightIndices;

        BestSplit(int featureIndex, double threshold, int[] leftIndices, int[] rightIndices) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
            this.leftIndices = leftIndices;
            this.rightIndices = rightIndices;
        }
    }
}
