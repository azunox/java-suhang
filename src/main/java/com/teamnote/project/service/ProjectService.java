package com.teamnote.project.service;

import java.util.List;

import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.dto.ProjectSearchRequest;
import com.teamnote.project.dto.ProjectUpdateRequest;

public interface ProjectService {

    ProjectResponse create(ProjectCreateRequest request);

    List<ProjectResponse> findAll(ProjectSearchRequest request);

    ProjectResponse findById(Long projectId);

    ProjectResponse update(Long projectId, ProjectUpdateRequest request);

    void delete(Long projectId);
}
