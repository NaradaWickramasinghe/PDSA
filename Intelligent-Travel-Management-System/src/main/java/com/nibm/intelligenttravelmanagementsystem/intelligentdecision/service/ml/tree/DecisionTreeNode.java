package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionTreeNode implements Serializable {

    private boolean isLeaf;
    private int featureIndex;
    private double threshold;
    private String featureName;

    private DecisionTreeNode leftChild;  // <= threshold
    private DecisionTreeNode rightChild; // > threshold

    private String predictedClass;
    private double confidence;
    private Map<String, Double> classDistribution;
    private int sampleCount;
    private double impurity;
}
