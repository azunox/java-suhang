package com.teamnote.project.controller;

import java.util.List;

import com.teamnote.common.response.ApiResponse;
import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(projectService.create(request)));
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> findAll() {
        return ApiResponse.success(projectService.findAll());
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> findById(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId
    ) {
        return ApiResponse.success(projectService.findById(projectId));
    }
}
