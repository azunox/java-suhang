package com.teamnote.task.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.project.entity.Project;
import com.teamnote.project.repository.ProjectRepository;
import com.teamnote.task.dto.TaskCreateRequest;
import com.teamnote.task.dto.TaskResponse;
import com.teamnote.task.entity.Task;
import com.teamnote.task.entity.TaskStatus;
import com.teamnote.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 10);

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private Project project;
    @InjectMocks
    private TaskServiceImpl service;

    @Test
    void createsDoneTaskUsingExistingProjectAndSeoulTime() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(project.getId()).thenReturn(1L);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "id", 10L);
            return task;
        });
        LocalDateTime before = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        TaskResponse result = service.create(1L,
                new TaskCreateRequest(" 업무 ", "  설명\n", " 담당자 ", DATE, TaskStatus.DONE));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.projectId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("업무");
        assertThat(result.descriptionMarkdown()).isEqualTo("  설명\n");
        assertThat(result.completedAt()).isBetween(before, LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }

    @Test
    void missingProjectCannotCreateTask() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(99L,
                new TaskCreateRequest("업무", "", "담당자", DATE, TaskStatus.TODO)))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(taskRepository);
    }

    @Test
    void rejectsMissingOrForeignTask() {
        when(taskRepository.findByIdAndProject_Id(10L, 2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(2L, 10L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnsOwnedTaskDetail() {
        Task task = ownedTask();
        when(project.getId()).thenReturn(1L);
        when(taskRepository.findByIdAndProject_Id(10L, 1L)).thenReturn(Optional.of(task));
        TaskResponse result = service.findById(1L, 10L);
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.projectId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("업무");
    }

    @Test
    void returnsProjectTasksInRepositoryDueDateAndIdOrder() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(project.getId()).thenReturn(1L);
        when(taskRepository.findByProject_IdOrderByDueDateAscIdAsc(1L)).thenReturn(List.of(ownedTask()));
        assertThat(service.findAll(1L)).extracting(TaskResponse::id).containsExactly(10L);
    }

    @Test
    void existingProjectWithoutTasksReturnsEmptyList() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByProject_IdOrderByDueDateAscIdAsc(1L)).thenReturn(List.of());
        assertThat(service.findAll(1L)).isEmpty();
    }

    @Test
    void missingProjectIsNotHiddenAsEmptyList() {
        when(projectRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.findAll(99L)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(taskRepository);
    }

    private Task ownedTask() {
        Task task = Task.create(project, "업무", "설명", "담당자", DATE, TaskStatus.TODO, DATE.atTime(14, 0));
        ReflectionTestUtils.setField(task, "id", 10L);
        return task;
    }
}
