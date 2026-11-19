package com.teamnote.project.service;

import java.util.List;

import com.teamnote.common.exception.ConflictException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.common.service.MarkdownService;
import com.teamnote.meeting.repository.MeetingRepository;
import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.dto.ProjectSearchRequest;
import com.teamnote.project.dto.ProjectUpdateRequest;
import com.teamnote.project.entity.Project;
import com.teamnote.project.repository.ProjectRepository;
import com.teamnote.task.repository.TaskRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final String PROJECT_HAS_CHILDREN = "업무 또는 회의가 남아 있는 프로젝트는 삭제할 수 없습니다.";

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final MarkdownService markdownService;

    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            MeetingRepository meetingRepository,
            MarkdownService markdownService
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.meetingRepository = meetingRepository;
        this.markdownService = markdownService;
    }

    @Override
    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        Project project = Project.create(
                request.name(),
                request.descriptionMarkdown(),
                request.startDate(),
                request.endDate(),
                request.status()
        );
        return toResponse(projectRepository.save(project));
    }

    @Override
    public List<ProjectResponse> findAll(ProjectSearchRequest request) {
        return projectRepository.search(toNamePattern(request.name()), request.projectStatus()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProjectResponse findById(Long projectId) {
        return toResponse(getProject(projectId));
    }

    @Override
    @Transactional
    public ProjectResponse update(Long projectId, ProjectUpdateRequest request) {
        Project project = getProject(projectId);
        project.update(request.name(), request.descriptionMarkdown(), request.startDate(), request.endDate(),
                request.status());
        return toResponse(project);
    }

    @Override
    @Transactional
    public void delete(Long projectId) {
        Project project = getProject(projectId);
        if (taskRepository.existsByProject_Id(projectId) || meetingRepository.existsByProject_Id(projectId)) {
            throw new ConflictException(PROJECT_HAS_CHILDREN);
        }

        try {
            projectRepository.delete(project);
            // 커밋 전에 FK 충돌을 확인해 API의 공통 충돌 응답으로 변환한다.
            projectRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            if (violatesProjectForeignKey(exception)) {
                throw new ConflictException(PROJECT_HAS_CHILDREN);
            }
            throw exception;
        } catch (ObjectOptimisticLockingFailureException exception) {
            if (Project.class.getName().equals(exception.getPersistentClassName())
                    && projectId.equals(exception.getIdentifier())) {
                throw new ResourceNotFoundException("프로젝트를 찾을 수 없습니다.");
            }
            throw exception;
        }
    }

    private Project getProject(Long projectId) {
        return projectRepository.findProjectById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));
    }

    private boolean violatesProjectForeignKey(DataIntegrityViolationException exception) {
        for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getKind() == ConstraintKind.FOREIGN_KEY
                    && violation.getConstraintName() != null) {
                String qualifiedName = violation.getConstraintName();
                String name = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
                return "fk_tasks_project".equalsIgnoreCase(name) || "fk_meetings_project".equalsIgnoreCase(name);
            }
        }
        return false;
    }

    private String toNamePattern(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String escapedName = name.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + escapedName + "%";
    }

    private ProjectResponse toResponse(Project project) {
        String descriptionMarkdown = project.getDescriptionMarkdown();
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                descriptionMarkdown,
                markdownService.render(descriptionMarkdown),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus()
        );
    }
}
