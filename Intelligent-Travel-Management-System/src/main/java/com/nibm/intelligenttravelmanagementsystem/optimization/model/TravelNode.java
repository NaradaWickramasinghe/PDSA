package com.nibm.intelligenttravelmanagementsystem.optimization.model;

public class TravelNode {
    private String nodeId;
    private String name;
    private String nodeType;
    private String province;
    private String district;
    private double latitude;
    private double longitude;
    private String description;

    public TravelNode() {}

    public TravelNode(String nodeId, String name, String nodeType, String province, String district, double latitude, double longitude, String description) {
        this.nodeId = nodeId;
        this.name = name;
        this.nodeType = nodeType;
        this.province = province;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public static TravelNodeBuilder builder() { return new TravelNodeBuilder(); }

    public static class TravelNodeBuilder {
        private String nodeId; private String name; private String nodeType;
        private String province; private String district; private double latitude;
        private double longitude; private String description;

        public TravelNodeBuilder nodeId(String id) { this.nodeId = id; return this; }
        public TravelNodeBuilder name(String name) { this.name = name; return this; }
        public TravelNodeBuilder nodeType(String t) { this.nodeType = t; return this; }
        public TravelNodeBuilder province(String p) { this.province = p; return this; }
        public TravelNodeBuilder district(String d) { this.district = d; return this; }
        public TravelNodeBuilder latitude(double lat) { this.latitude = lat; return this; }
        public TravelNodeBuilder longitude(double lon) { this.longitude = lon; return this; }
        public TravelNodeBuilder description(String desc) { this.description = desc; return this; }
        public TravelNode build() { return new TravelNode(nodeId, name, nodeType, province, district, latitude, longitude, description); }
    }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
