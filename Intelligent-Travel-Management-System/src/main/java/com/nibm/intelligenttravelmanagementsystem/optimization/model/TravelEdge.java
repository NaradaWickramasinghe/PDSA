package com.nibm.intelligenttravelmanagementsystem.optimization.model;

public class TravelEdge {
    private String edgeId;
    private String source;
    private String destination;
    private double distanceKm;
    private int travelTimeMinutes;
    private int estimatedCostLkr;
    private int roadQuality;
    private int trafficLevel;
    private String transportMode;
    private int accessibility;
    private int riskLevel;

    public TravelEdge() {}

    public TravelEdge(String edgeId, String source, String destination, double distanceKm, int travelTimeMinutes, int estimatedCostLkr, int roadQuality, int trafficLevel, String transportMode, int accessibility, int riskLevel) {
        this.edgeId = edgeId;
        this.source = source;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.travelTimeMinutes = travelTimeMinutes;
        this.estimatedCostLkr = estimatedCostLkr;
        this.roadQuality = roadQuality;
        this.trafficLevel = trafficLevel;
        this.transportMode = transportMode;
        this.accessibility = accessibility;
        this.riskLevel = riskLevel;
    }

    public static TravelEdgeBuilder builder() { return new TravelEdgeBuilder(); }

    public static class TravelEdgeBuilder {
        private String edgeId; private String source; private String destination;
        private double distanceKm; private int travelTimeMinutes; private int estimatedCostLkr;
        private int roadQuality; private int trafficLevel; private String transportMode;
        private int accessibility; private int riskLevel;

        public TravelEdgeBuilder edgeId(String id) { this.edgeId = id; return this; }
        public TravelEdgeBuilder source(String s) { this.source = s; return this; }
        public TravelEdgeBuilder destination(String d) { this.destination = d; return this; }
        public TravelEdgeBuilder distanceKm(double dist) { this.distanceKm = dist; return this; }
        public TravelEdgeBuilder travelTimeMinutes(int t) { this.travelTimeMinutes = t; return this; }
        public TravelEdgeBuilder estimatedCostLkr(int c) { this.estimatedCostLkr = c; return this; }
        public TravelEdgeBuilder roadQuality(int q) { this.roadQuality = q; return this; }
        public TravelEdgeBuilder trafficLevel(int tr) { this.trafficLevel = tr; return this; }
        public TravelEdgeBuilder transportMode(String m) { this.transportMode = m; return this; }
        public TravelEdgeBuilder accessibility(int a) { this.accessibility = a; return this; }
        public TravelEdgeBuilder riskLevel(int r) { this.riskLevel = r; return this; }
        public TravelEdge build() { return new TravelEdge(edgeId, source, destination, distanceKm, travelTimeMinutes, estimatedCostLkr, roadQuality, trafficLevel, transportMode, accessibility, riskLevel); }
    }

    public double getEffectiveTravelTime() {
        double trafficMultiplier = 1.0 + ((trafficLevel - 1) * 0.15);
        return travelTimeMinutes * trafficMultiplier;
    }

    public String getEdgeId() { return edgeId; }
    public void setEdgeId(String edgeId) { this.edgeId = edgeId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(int travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }

    public int getEstimatedCostLkr() { return estimatedCostLkr; }
    public void setEstimatedCostLkr(int estimatedCostLkr) { this.estimatedCostLkr = estimatedCostLkr; }

    public int getRoadQuality() { return roadQuality; }
    public void setRoadQuality(int roadQuality) { this.roadQuality = roadQuality; }

    public int getTrafficLevel() { return trafficLevel; }
    public void setTrafficLevel(int trafficLevel) { this.trafficLevel = trafficLevel; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }

    public int getAccessibility() { return accessibility; }
    public void setAccessibility(int accessibility) { this.accessibility = accessibility; }

    public int getRiskLevel() { return riskLevel; }
    public void setRiskLevel(int riskLevel) { this.riskLevel = riskLevel; }
}
