package com.teamnote.project.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.teamnote.common.config.MarkdownConfiguration;
import com.teamnote.common.exception.ConflictException;
import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.common.service.MarkdownServiceImpl;
import com.teamnote.meeting.repository.MeetingRepository;
import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.dto.ProjectSearchRequest;
import com.teamnote.project.dto.ProjectUpdateRequest;
import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.repository.ProjectRepository;
import com.teamnote.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 11, 9);
    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 20);
    private static final String MARKDOWN = "  # TEAMNOTE\n\n설명  \n";
    private static final String HTML = "<h1>TEAMNOTE</h1>\n<p>설명</p>";

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MeetingRepository meetingRepository;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MarkdownConfiguration configuration = new MarkdownConfiguration();
        projectService = new ProjectServiceImpl(projectRepository, taskRepository, meetingRepository, new MarkdownServiceImpl(
                configuration.markdownParser(), configuration.markdownHtmlRenderer(), configuration.markdownHtmlCleaner()
        ));
    }

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
        assertThat(response).isEqualTo(new ProjectResponse(1L, "REST API 수행평가", MARKDOWN, HTML,
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
        when(projectRepository.search(null, null)).thenReturn(projects);

        List<ProjectResponse> responses = projectService.findAll(new ProjectSearchRequest(null, null));

        assertThat(responses).extracting(ProjectResponse::id).containsExactly(1L, 2L);
        assertThat(responses).extracting(ProjectResponse::descriptionHtml).containsExactly(HTML, HTML);
        assertThat(responses).extracting(ProjectResponse::name).containsExactly("첫 프로젝트", "두 번째 프로젝트");
        verify(projectRepository).search(null, null);
    }

    @Test
    void returnsEmptyListWhenNoProjectsExist() {
        when(projectRepository.search(null, ProjectStatus.PLANNED)).thenReturn(List.of());

        assertThat(projectService.findAll(new ProjectSearchRequest("   ", "PLANNED"))).isEmpty();
        verify(projectRepository).search(null, ProjectStatus.PLANNED);
    }

    @Test
    void escapesNameWildcardsAndPassesStatusToQuery() {
        ProjectSearchRequest request = new ProjectSearchRequest("  API_50%!  ", "IN_PROGRESS");

        projectService.findAll(request);

        verify(projectRepository).search("%API!_50!%!!%", ProjectStatus.IN_PROGRESS);
    }

    @Test
    void returnsExistingProject() {
        Project project = storedProject(1L, "프로젝트");
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.findById(1L);

        assertThat(response).isEqualTo(new ProjectResponse(1L, "프로젝트", MARKDOWN, HTML,
                START_DATE, END_DATE, ProjectStatus.IN_PROGRESS));
    }

    @Test
    void rejectsMissingProject() {
        when(projectRepository.findProjectById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }

    @Test
    void updatesAllFieldsAndRendersChangedDescription() {
        Project project = Project.create("기존 프로젝트", "", START_DATE, END_DATE, ProjectStatus.PLANNED);
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));
        ProjectUpdateRequest request = new ProjectUpdateRequest("  변경된 프로젝트  ", MARKDOWN,
                END_DATE, END_DATE, ProjectStatus.COMPLETED);

        ProjectResponse response = projectService.update(1L, request);

        assertThat(response).isEqualTo(new ProjectResponse(null, "변경된 프로젝트", MARKDOWN, HTML,
                END_DATE, END_DATE, ProjectStatus.COMPLETED));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidUpdateWithoutChangingAnyFields() {
        Project project = Project.create("프로젝트", MARKDOWN, START_DATE, END_DATE, ProjectStatus.IN_PROGRESS);
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));
        ProjectResponse before = projectService.findById(1L);
        ProjectUpdateRequest request = new ProjectUpdateRequest("변경된 프로젝트", "",
                END_DATE, START_DATE, ProjectStatus.COMPLETED);

        assertThatThrownBy(() -> projectService.update(1L, request)).isInstanceOf(InvalidRequestException.class);
        assertThat(projectService.findById(1L)).isEqualTo(before);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    void rejectsDeletionWhenTaskOrMeetingExists(boolean hasTask, boolean hasMeeting) {
        Project project = Project.create("프로젝트", "", START_DATE, END_DATE, ProjectStatus.PLANNED);
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.existsByProject_Id(1L)).thenReturn(hasTask);
        if (!hasTask) {
            when(meetingRepository.existsByProject_Id(1L)).thenReturn(hasMeeting);
        }

        assertThatThrownBy(() -> projectService.delete(1L)).isInstanceOf(ConflictException.class);
        verify(projectRepository, never()).delete(any(Project.class));
        verify(projectRepository, never()).flush();
    }

    @Test
    void deletesOnlyTheProjectAndFlushesWhenNoChildrenExist() {
        Project project = Project.create("프로젝트", "", START_DATE, END_DATE, ProjectStatus.PLANNED);
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        var order = inOrder(taskRepository, meetingRepository, projectRepository);
        order.verify(taskRepository).existsByProject_Id(1L);
        order.verify(meetingRepository).existsByProject_Id(1L);
        order.verify(projectRepository).delete(project);
        order.verify(projectRepository).flush();
    }

    @Test
    void rejectsDeletionOfMissingProjectBeforeCheckingChildren() {
        when(projectRepository.findProjectById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(999L)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(taskRepository, meetingRepository);
    }

    @Test
    void propagatesUnrelatedDatabaseErrorsFromDelete() {
        Project project = Project.create("프로젝트", "", START_DATE, END_DATE, ProjectStatus.PLANNED);
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));
        DataIntegrityViolationException failure = new DataIntegrityViolationException("별도 제약 위반");
        doThrow(failure).when(projectRepository).flush();

        assertThatThrownBy(() -> projectService.delete(1L)).isSameAs(failure);
    }

    @Test
    void reportsMissingProjectWhenAnotherDeleteRemovedTheSameRow() {
        Project project = Project.create("프로젝트", "", START_DATE, END_DATE, ProjectStatus.PLANNED);
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(project));
        doThrow(new ObjectOptimisticLockingFailureException(Project.class, 1L)).when(projectRepository).flush();

        assertThatThrownBy(() -> projectService.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
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
