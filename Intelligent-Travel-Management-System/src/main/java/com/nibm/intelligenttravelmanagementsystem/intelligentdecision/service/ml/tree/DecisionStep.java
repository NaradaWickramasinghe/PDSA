package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Represents a single decision step traversed along a Decision Tree branch.
 * Pushed onto the {@link CustomStack} during model inference for explainability and backtracking.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DecisionStep implements Serializable {

    private DecisionTreeNode node;
    private int featureIndex;
    private String featureName;
    private double featureValue;
    private double threshold;
    private boolean branchLeft; // true if value <= threshold, false if value > threshold
    private String ruleDescription;

    /**
     * Formats this step into a human-readable rule condition.
     * Example: "BeachPreference (4.50) > 3.00" or "Budget (450.00) <= 800.00"
     */
    public String getFormattedRule() {
        if (ruleDescription != null && !ruleDescription.isEmpty()) {
            return ruleDescription;
        }
        String operator = branchLeft ? "<=" : ">";
        return String.format("%s (%.2f) %s %.2f", featureName, featureValue, operator, threshold);
    }
}
