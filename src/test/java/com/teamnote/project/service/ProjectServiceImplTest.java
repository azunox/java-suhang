package com.teamnote.project.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 11, 9);
    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 20);
    private static final String MARKDOWN = "  # TEAMNOTE\n\n설명  \n";

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void savesNormalizedProjectAndReturnsPersistedValues() {
        Project savedProject = storedProject(1L, "REST API 수행평가");
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        ProjectCreateRequest request = new ProjectCreateRequest("  REST API 수행평가  ", MARKDOWN,
                START_DATE, END_DATE, ProjectStatus.IN_PROGRESS);

        ProjectResponse response = projectService.create(request);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("REST API 수행평가");
        assertThat(captor.getValue().getDescriptionMarkdown()).isEqualTo(MARKDOWN);
        assertThat(response).isEqualTo(new ProjectResponse(1L, "REST API 수행평가", MARKDOWN,
                START_DATE, END_DATE, ProjectStatus.IN_PROGRESS));
    }

    @Test
    void rejectsReversedDatesWithoutSaving() {
        ProjectCreateRequest request = new ProjectCreateRequest("프로젝트", "",
                END_DATE, START_DATE, ProjectStatus.PLANNED);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(projectRepository);
    }

    @Test
    void returnsProjectDtosUsingDatabaseIdOrder() {
        List<Project> projects = List.of(storedProject(1L, "첫 프로젝트"), storedProject(2L, "두 번째 프로젝트"));
        when(projectRepository.findAllProjects()).thenReturn(projects);

        List<ProjectResponse> responses = projectService.findAll();

        assertThat(responses).extracting(ProjectResponse::id).containsExactly(1L, 2L);
        assertThat(responses).extracting(ProjectResponse::name).containsExactly("첫 프로젝트", "두 번째 프로젝트");
        verify(projectRepository).findAllProjects();
    }

    @Test
    void returnsEmptyListWhenNoProjectsExist() {
        when(projectRepository.findAllProjects()).thenReturn(List.of());

        assertThat(projectService.findAll()).isEmpty();
    }

    @Test
    void returnsExistingProject() {
        Project project = storedProject(1L, "프로젝트");
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.findById(1L);

        assertThat(response).isEqualTo(new ProjectResponse(1L, "프로젝트", MARKDOWN,
                START_DATE, END_DATE, ProjectStatus.IN_PROGRESS));
    }

    @Test
    void rejectsMissingProject() {
        when(projectRepository.findProjectById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }

    private Project storedProject(Long id, String name) {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(id);
        when(project.getName()).thenReturn(name);
        when(project.getDescriptionMarkdown()).thenReturn(MARKDOWN);
        when(project.getStartDate()).thenReturn(START_DATE);
        when(project.getEndDate()).thenReturn(END_DATE);
        when(project.getStatus()).thenReturn(ProjectStatus.IN_PROGRESS);
        return project;
    }
}
