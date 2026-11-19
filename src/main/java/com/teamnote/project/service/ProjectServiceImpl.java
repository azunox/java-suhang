package com.teamnote.project.service;

import java.util.List;

import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.common.service.MarkdownService;
import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.dto.ProjectSearchRequest;
import com.teamnote.project.entity.Project;
import com.teamnote.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final MarkdownService markdownService;

    public ProjectServiceImpl(ProjectRepository projectRepository, MarkdownService markdownService) {
        this.projectRepository = projectRepository;
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
        Project project = projectRepository.findProjectById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));
        return toResponse(project);
    }

    private String toNamePattern(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String escapedName = name.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + escapedName + "%";
    }

    private ProjectResponse toResponse(Project project) {
        String markdown = project.getDescriptionMarkdown();
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                markdown,
                markdownService.render(markdown),
                project.getStartDate(),
                project.getEndDate(),
                project.getStatus()
        );
    }
}
