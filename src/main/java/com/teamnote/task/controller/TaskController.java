package com.teamnote.task.controller;

import java.util.List;

import com.teamnote.common.response.ApiResponse;
import com.teamnote.task.dto.TaskCreateRequest;
import com.teamnote.task.dto.TaskResponse;
import com.teamnote.task.dto.TaskUpdateRequest;
import com.teamnote.task.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @PathVariable @Positive Long projectId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taskService.create(projectId, request)));
    }

    @GetMapping
    public ApiResponse<List<TaskResponse>> findAll(
            @PathVariable @Positive Long projectId
    ) {
        return ApiResponse.success(taskService.findAll(projectId));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponse> findById(
            @PathVariable @Positive Long projectId,
            @PathVariable @Positive Long taskId
    ) {
        return ApiResponse.success(taskService.findById(projectId, taskId));
    }

    @PutMapping("/{taskId}")
    public ApiResponse<TaskResponse> update(
            @PathVariable @Positive Long projectId,
            @PathVariable @Positive Long taskId,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        return ApiResponse.success(taskService.update(projectId, taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> delete(
            @PathVariable @Positive Long projectId,
            @PathVariable @Positive Long taskId
    ) {
        taskService.delete(projectId, taskId);
        return ApiResponse.success(null);
    }
}
