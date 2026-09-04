package com.nibm.intelligenttravelmanagementsystem.optimization.dto;

import java.util.List;

public class OptimizationRequest {
    private String sourceNodeId;
    private String destinationNodeId;

    private Integer maxBudgetLkr;
    private Integer maxTimeMinutes;
    private Integer maxAllowedRisk;
    private List<String> preferredModes;

    private double timeWeight = 0.35;
    private double costWeight = 0.35;
    private double safetyWeight = 0.20;
    private double qualityWeight = 0.10;

    private AlgorithmType algorithm = AlgorithmType.BRANCH_AND_BOUND;

    // Multi-Module synthesis fields (takes inputs for Modules 1, 2, 3, 4 automatically)
    private String travelStyle = "ADVENTURE";
    private Integer beachPreference = 7;
    private Integer adventurePreference = 8;
    private Integer naturePreference = 9;
    private Integer culturePreference = 6;
    private Integer nightlifePreference = 4;
    private Integer relaxationPreference = 8;
    private Integer groupSize = 2;
    private Integer durationDays = 3;
    private boolean autoFetchFromModules = true;

    public OptimizationRequest() {}

    public static OptimizationRequestBuilder builder() { return new OptimizationRequestBuilder(); }

    public static class OptimizationRequestBuilder {
        private String sourceNodeId;
        private String destinationNodeId;
        private Integer maxBudgetLkr;
        private Integer maxTimeMinutes;
        private Integer maxAllowedRisk;
        private List<String> preferredModes;
        private double timeWeight = 0.35;
        private double costWeight = 0.35;
        private double safetyWeight = 0.20;
        private double qualityWeight = 0.10;
        private AlgorithmType algorithm = AlgorithmType.BRANCH_AND_BOUND;

        private String travelStyle = "ADVENTURE";
        private Integer beachPreference = 7;
        private Integer adventurePreference = 8;
        private Integer naturePreference = 9;
        private Integer culturePreference = 6;
        private Integer nightlifePreference = 4;
        private Integer relaxationPreference = 8;
        private Integer groupSize = 2;
        private Integer durationDays = 3;
        private boolean autoFetchFromModules = true;

        public OptimizationRequestBuilder sourceNodeId(String src) { this.sourceNodeId = src; return this; }
        public OptimizationRequestBuilder destinationNodeId(String dst) { this.destinationNodeId = dst; return this; }
        public OptimizationRequestBuilder maxBudgetLkr(Integer b) { this.maxBudgetLkr = b; return this; }
        public OptimizationRequestBuilder maxTimeMinutes(Integer t) { this.maxTimeMinutes = t; return this; }
        public OptimizationRequestBuilder maxAllowedRisk(Integer r) { this.maxAllowedRisk = r; return this; }
        public OptimizationRequestBuilder preferredModes(List<String> m) { this.preferredModes = m; return this; }
        public OptimizationRequestBuilder timeWeight(double tw) { this.timeWeight = tw; return this; }
        public OptimizationRequestBuilder costWeight(double cw) { this.costWeight = cw; return this; }
        public OptimizationRequestBuilder safetyWeight(double sw) { this.safetyWeight = sw; return this; }
        public OptimizationRequestBuilder qualityWeight(double qw) { this.qualityWeight = qw; return this; }
        public OptimizationRequestBuilder algorithm(AlgorithmType algo) { this.algorithm = algo; return this; }

        public OptimizationRequestBuilder travelStyle(String style) { this.travelStyle = style; return this; }
        public OptimizationRequestBuilder beachPreference(Integer b) { this.beachPreference = b; return this; }
        public OptimizationRequestBuilder adventurePreference(Integer a) { this.adventurePreference = a; return this; }
        public OptimizationRequestBuilder naturePreference(Integer n) { this.naturePreference = n; return this; }
        public OptimizationRequestBuilder culturePreference(Integer c) { this.culturePreference = c; return this; }
        public OptimizationRequestBuilder nightlifePreference(Integer nl) { this.nightlifePreference = nl; return this; }
        public OptimizationRequestBuilder relaxationPreference(Integer r) { this.relaxationPreference = r; return this; }
        public OptimizationRequestBuilder groupSize(Integer g) { this.groupSize = g; return this; }
        public OptimizationRequestBuilder durationDays(Integer d) { this.durationDays = d; return this; }
        public OptimizationRequestBuilder autoFetchFromModules(boolean af) { this.autoFetchFromModules = af; return this; }

        public OptimizationRequest build() {
            OptimizationRequest req = new OptimizationRequest();
            req.sourceNodeId = this.sourceNodeId;
            req.destinationNodeId = this.destinationNodeId;
            req.maxBudgetLkr = this.maxBudgetLkr;
            req.maxTimeMinutes = this.maxTimeMinutes;
            req.maxAllowedRisk = this.maxAllowedRisk;
            req.preferredModes = this.preferredModes;
            req.timeWeight = this.timeWeight;
            req.costWeight = this.costWeight;
            req.safetyWeight = this.safetyWeight;
            req.qualityWeight = this.qualityWeight;
            req.algorithm = this.algorithm;
            req.travelStyle = this.travelStyle;
            req.beachPreference = this.beachPreference;
            req.adventurePreference = this.adventurePreference;
            req.naturePreference = this.naturePreference;
            req.culturePreference = this.culturePreference;
            req.nightlifePreference = this.nightlifePreference;
            req.relaxationPreference = this.relaxationPreference;
            req.groupSize = this.groupSize;
            req.durationDays = this.durationDays;
            req.autoFetchFromModules = this.autoFetchFromModules;
            return req;
        }
    }

    public void normalizeWeights() {
        double total = timeWeight + costWeight + safetyWeight + qualityWeight;
        if (total <= 0.0001) {
            timeWeight = 0.35;
            costWeight = 0.35;
            safetyWeight = 0.20;
            qualityWeight = 0.10;
        } else {
            timeWeight /= total;
            costWeight /= total;
            safetyWeight /= total;
            qualityWeight /= total;
        }
    }

    // Getters and Setters
    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public String getDestinationNodeId() { return destinationNodeId; }
    public void setDestinationNodeId(String destinationNodeId) { this.destinationNodeId = destinationNodeId; }

    public Integer getMaxBudgetLkr() { return maxBudgetLkr; }
    public void setMaxBudgetLkr(Integer maxBudgetLkr) { this.maxBudgetLkr = maxBudgetLkr; }

    public Integer getMaxTimeMinutes() { return maxTimeMinutes; }
    public void setMaxTimeMinutes(Integer maxTimeMinutes) { this.maxTimeMinutes = maxTimeMinutes; }

    public Integer getMaxAllowedRisk() { return maxAllowedRisk; }
    public void setMaxAllowedRisk(Integer maxAllowedRisk) { this.maxAllowedRisk = maxAllowedRisk; }

    public List<String> getPreferredModes() { return preferredModes; }
    public void setPreferredModes(List<String> preferredModes) { this.preferredModes = preferredModes; }

    public double getTimeWeight() { return timeWeight; }
    public void setTimeWeight(double timeWeight) { this.timeWeight = timeWeight; }

    public double getCostWeight() { return costWeight; }
    public void setCostWeight(double costWeight) { this.costWeight = costWeight; }

    public double getSafetyWeight() { return safetyWeight; }
    public void setSafetyWeight(double safetyWeight) { this.safetyWeight = safetyWeight; }

    public double getQualityWeight() { return qualityWeight; }
    public void setQualityWeight(double qualityWeight) { this.qualityWeight = qualityWeight; }

    public AlgorithmType getAlgorithm() { return algorithm; }
    public void setAlgorithm(AlgorithmType algorithm) { this.algorithm = algorithm; }

    public String getTravelStyle() { return travelStyle; }
    public void setTravelStyle(String travelStyle) { this.travelStyle = travelStyle; }

    public Integer getBeachPreference() { return beachPreference; }
    public void setBeachPreference(Integer beachPreference) { this.beachPreference = beachPreference; }

    public Integer getAdventurePreference() { return adventurePreference; }
    public void setAdventurePreference(Integer adventurePreference) { this.adventurePreference = adventurePreference; }

    public Integer getNaturePreference() { return naturePreference; }
    public void setNaturePreference(Integer naturePreference) { this.naturePreference = naturePreference; }

    public Integer getCulturePreference() { return culturePreference; }
    public void setCulturePreference(Integer culturePreference) { this.culturePreference = culturePreference; }

    public Integer getNightlifePreference() { return nightlifePreference; }
    public void setNightlifePreference(Integer nightlifePreference) { this.nightlifePreference = nightlifePreference; }

    public Integer getRelaxationPreference() { return relaxationPreference; }
    public void setRelaxationPreference(Integer relaxationPreference) { this.relaxationPreference = relaxationPreference; }

    public Integer getGroupSize() { return groupSize; }
    public void setGroupSize(Integer groupSize) { this.groupSize = groupSize; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public boolean isAutoFetchFromModules() { return autoFetchFromModules; }
    public void setAutoFetchFromModules(boolean autoFetchFromModules) { this.autoFetchFromModules = autoFetchFromModules; }
}
