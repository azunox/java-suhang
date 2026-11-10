package com.teamnote.project.service;

import java.util.List;

import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;

public interface ProjectService {

    ProjectResponse create(ProjectCreateRequest request);

    List<ProjectResponse> findAll();

    ProjectResponse findById(Long projectId);
}
