package com.teamnote.task.service;

import java.util.List;

import com.teamnote.task.dto.TaskCreateRequest;
import com.teamnote.task.dto.TaskResponse;

public interface TaskService {

    TaskResponse create(Long projectId, TaskCreateRequest request);

    List<TaskResponse> findAll(Long projectId);

    TaskResponse findById(Long projectId, Long taskId);

}
