package com.teamnote.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.teamnote.common.config.MarkdownConfiguration;
import com.teamnote.common.exception.ConflictException;
import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.common.service.MarkdownServiceImpl;
import com.teamnote.meeting.entity.Meeting;
import com.teamnote.meeting.repository.MeetingRepository;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.dto.ProjectUpdateRequest;
import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.repository.ProjectRepository;
import com.teamnote.task.entity.Task;
import com.teamnote.task.entity.TaskStatus;
import com.teamnote.task.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@DataJpaTest
@Import({ProjectServiceImpl.class, MarkdownServiceImpl.class, MarkdownConfiguration.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProjectPersistenceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 19);
    private static final LocalDateTime PAST = LocalDateTime.of(2026, 11, 10, 9, 0);

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    @MockitoSpyBean
    private TaskRepository taskRepository;

    @MockitoSpyBean
    private MeetingRepository meetingRepository;

    private Project project;
    private Long taskId;
    private Long meetingId;

    @Autowired
    ProjectPersistenceTest(ProjectService projectService, ProjectRepository projectRepository) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
    }

    @BeforeEach
    void setUp() {
        project = projectRepository.save(Project.create("프로젝트", "# 원문\n", DATE, DATE, ProjectStatus.PLANNED));
    }

    @AfterEach
    void cleanUp() {
        if (taskId != null) {
            taskRepository.deleteById(taskId);
        }
        if (meetingId != null) {
            meetingRepository.deleteById(meetingId);
        }
        if (project != null) {
            projectRepository.deleteById(project.getId());
        }
    }

    @Test
    void commitsValidUpdateAndPreservesCommittedValuesAfterInvalidUpdate() {
        Long id = project.getId();
        ProjectUpdateRequest valid = new ProjectUpdateRequest("  변경  ", "## 변경\n", DATE, DATE,
                ProjectStatus.IN_PROGRESS);
        ProjectResponse updated = projectService.update(id, valid);
        assertThat(projectService.findById(id)).isEqualTo(updated);
        assertThat(updated.name()).isEqualTo("변경");
        assertThat(updated.descriptionHtml()).isEqualTo("<h2>변경</h2>");

        ProjectUpdateRequest invalid = new ProjectUpdateRequest("실패", "", DATE.plusDays(1), DATE,
                ProjectStatus.COMPLETED);
        assertThatThrownBy(() -> projectService.update(id, invalid)).isInstanceOf(InvalidRequestException.class);
        assertThat(projectService.findById(id)).isEqualTo(updated);
    }

    @Test
    void blocksDeletionUntilDoneTaskAndPastMeetingAreBothRemoved() {
        Long id = project.getId();
        saveDoneTask();
        savePastMeeting();

        assertThatThrownBy(() -> projectService.delete(id)).isInstanceOf(ConflictException.class);
        assertThat(taskRepository.existsById(taskId)).isTrue();
        assertThat(meetingRepository.existsById(meetingId)).isTrue();
        assertThat(projectRepository.existsById(id)).isTrue();

        taskRepository.deleteById(taskId);
        taskId = null;
        assertThatThrownBy(() -> projectService.delete(id)).isInstanceOf(ConflictException.class);
        assertThat(meetingRepository.existsById(meetingId)).isTrue();
        assertThat(projectRepository.existsById(id)).isTrue();

        meetingRepository.deleteById(meetingId);
        meetingId = null;
        projectService.delete(id);
        assertThatThrownBy(() -> projectService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void mapsTaskForeignKeyFailureAndRollsBackWhenPrecheckMissesChild() {
        Long id = project.getId();
        saveDoneTask();
        doReturn(false).when(taskRepository).existsByProject_Id(id);

        assertThatThrownBy(() -> projectService.delete(id)).isInstanceOf(ConflictException.class);
        assertThat(projectRepository.existsById(id)).isTrue();
        assertThat(taskRepository.existsById(taskId)).isTrue();
    }

    @Test
    void mapsMeetingForeignKeyFailureAndRollsBackWhenPrecheckMissesChild() {
        Long id = project.getId();
        savePastMeeting();
        doReturn(false).when(meetingRepository).existsByProject_Id(id);

        assertThatThrownBy(() -> projectService.delete(id)).isInstanceOf(ConflictException.class);
        assertThat(projectRepository.existsById(id)).isTrue();
        assertThat(meetingRepository.existsById(meetingId)).isTrue();
    }

    @Test
    void concurrentDeletesReportSuccessAndNotFound() throws Exception {
        Long id = project.getId();
        assertThat(meetingRepository.existsByProject_Id(id)).isFalse();
        CyclicBarrier bothRequestsPassedExistenceCheck = new CyclicBarrier(2);
        doAnswer(invocation -> {
            bothRequestsPassedExistenceCheck.await(5, TimeUnit.SECONDS);
            return false;
        }).when(meetingRepository).existsByProject_Id(id);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> deleteIfPresent(id));
            var second = executor.submit(() -> deleteIfPresent(id));
            assertThat(new Boolean[]{first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(projectRepository.existsById(id)).isFalse();
    }

    private boolean deleteIfPresent(Long id) {
        try {
            projectService.delete(id);
            return true;
        } catch (ResourceNotFoundException exception) {
            return false;
        }
    }

    private void saveDoneTask() {
        taskId = taskRepository.save(Task.create(project, "완료 업무", "", "팀장", PAST.toLocalDate(),
                TaskStatus.DONE, PAST)).getId();
    }

    private void savePastMeeting() {
        meetingId = meetingRepository.save(Meeting.create(project, "과거 회의", PAST, PAST.plusHours(1), "")).getId();
    }
}
