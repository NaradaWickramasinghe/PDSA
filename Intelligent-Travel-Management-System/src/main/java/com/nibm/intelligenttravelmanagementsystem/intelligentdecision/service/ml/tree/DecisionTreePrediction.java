package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.SuitabilityLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DecisionTreePrediction {

    private String predictedClass;
    private double confidenceScore;
    private SuitabilityLabel suitabilityLabel;
    private Map<String, Double> classProbabilities;
    private List<String> decisionPathRules;
    private String rationale;
}
