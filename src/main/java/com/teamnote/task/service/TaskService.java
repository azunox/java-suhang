package com.teamnote.task.service;

import java.util.List;

import com.teamnote.task.dto.TaskCreateRequest;
import com.teamnote.task.dto.TaskResponse;
import com.teamnote.task.dto.TaskUpdateRequest;

public interface TaskService {

    TaskResponse create(Long projectId, TaskCreateRequest request);

    List<TaskResponse> findAll(Long projectId);

    TaskResponse findById(Long projectId, Long taskId);

    TaskResponse update(Long projectId, Long taskId, TaskUpdateRequest request);

    void delete(Long projectId, Long taskId);
}
