package com.nibm.intelligenttravelmanagementsystem.resourceallocation.service;

import com.nibm.intelligenttravelmanagementsystem.resourceallocation.model.ResourceOption;

import java.util.List;

public interface ResourceDataProvider {
    List<ResourceOption> getCandidateOptions();

    default List<ResourceOption> getCandidateOptions(String destination) {
        return getCandidateOptions();
    }
}
