package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service;

import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.model.TravelerProfile;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree.DecisionTreePrediction;
import com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.preprocessing.TravelerFeatureRecord;

import java.util.List;

public interface DecisionTreeService {

    DecisionTreePrediction predict(TravelerFeatureRecord record);

    DecisionTreePrediction predict(TravelerProfile profile);

    void trainModel(List<TravelerFeatureRecord> dataset);

    boolean isModelReady();
}
