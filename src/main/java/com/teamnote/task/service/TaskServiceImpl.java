package com.teamnote.task.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.project.entity.Project;
import com.teamnote.project.repository.ProjectRepository;
import com.teamnote.task.dto.TaskCreateRequest;
import com.teamnote.task.dto.TaskResponse;
import com.teamnote.task.dto.TaskUpdateRequest;
import com.teamnote.task.entity.Task;
import com.teamnote.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public TaskResponse create(Long projectId, TaskCreateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));
        LocalDateTime now = LocalDateTime.now(ZONE);
        Task task = Task.create(project, request.title(), request.descriptionMarkdown(),
                request.assigneeName(), request.dueDate(), request.status(), now);
        return toResponse(taskRepository.save(task));
    }

    @Override
    public List<TaskResponse> findAll(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("프로젝트를 찾을 수 없습니다.");
        }
        return taskRepository.findByProject_IdOrderByDueDateAscIdAsc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TaskResponse findById(Long projectId, Long taskId) {
        return toResponse(findTask(projectId, taskId));
    }

    @Override
    @Transactional
    public TaskResponse update(Long projectId, Long taskId, TaskUpdateRequest request) {
        Task task = findTask(projectId, taskId);
        LocalDateTime now = LocalDateTime.now(ZONE);
        task.update(request.title(), request.descriptionMarkdown(), request.assigneeName(),
                request.dueDate(), request.status(), now);
        return toResponse(task);
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long taskId) {
        taskRepository.delete(findTask(projectId, taskId));
    }

    private Task findTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProject_Id(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("업무를 찾을 수 없습니다."));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getProject().getId(), task.getTitle(),
                task.getDescriptionMarkdown(), task.getAssigneeName(), task.getDueDate(),
                task.getStatus(), task.getCompletedAt());
    }
}
