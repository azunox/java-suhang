package com.teamnote.task.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.repository.ProjectRepository;
import com.teamnote.task.entity.Task;
import com.teamnote.task.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);
    private static final LocalDateTime NOW = TODAY.atTime(10, 0);

    private final TaskRepository tasks;
    private final ProjectRepository projects;

    @Autowired
    TaskRepositoryTest(TaskRepository tasks, ProjectRepository projects) {
        this.tasks = tasks;
        this.projects = projects;
    }

    private Project project;
    private Project other;

    @BeforeEach
    void prepareProjects() {
        project = projects.save(Project.create("검색 검증", "", TODAY, TODAY, ProjectStatus.IN_PROGRESS));
        other = projects.save(Project.create("다른 프로젝트", "", TODAY, TODAY, ProjectStatus.PLANNED));
    }

    @Test
    void combinesAssigneeStatusAndInclusiveDateBoundsWithinProject() {
        Task first = save(project, "하한", "팀원 A", TODAY.minusDays(1), TaskStatus.TODO);
        Task second = save(project, "상한", "팀원 A", TODAY, TaskStatus.TODO);
        save(project, "다른 담당자", "팀원 B", TODAY, TaskStatus.TODO);
        save(project, "다른 상태", "팀원 A", TODAY, TaskStatus.DONE);
        save(project, "범위 밖", "팀원 A", TODAY.plusDays(1), TaskStatus.TODO);
        save(other, "다른 소속", "팀원 A", TODAY, TaskStatus.TODO);
        tasks.flush();

        assertThat(tasks.search(project.getId(), "팀원 A", TaskStatus.TODO,
                TODAY.minusDays(1), TODAY, null, TODAY, TaskStatus.DONE))
                .extracting(Task::getId).containsExactly(first.getId(), second.getId());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void distinguishesOverdueFromTodayFutureAndCompletedTasks(Boolean overdue) {
        Task late = save(project, "지난 미완료", "A", TODAY.minusDays(1), TaskStatus.TODO);
        Task done = save(project, "지난 완료", "A", TODAY.minusDays(1), TaskStatus.DONE);
        Task today = save(project, "오늘 마감", "A", TODAY, TaskStatus.TODO);
        Task future = save(project, "미래 마감", "A", TODAY.plusDays(1), TaskStatus.IN_PROGRESS);
        save(other, "다른 소속", "A", TODAY.minusDays(1), TaskStatus.TODO);
        tasks.flush();
        List<Task> expected = overdue == null ? List.of(late, done, today, future)
                : overdue ? List.of(late) : List.of(done, today, future);

        assertThat(tasks.search(project.getId(), null, null, null, null, overdue, TODAY, TaskStatus.DONE))
                .extracting(Task::getId).containsExactlyElementsOf(expected.stream().map(Task::getId).toList());
    }

    @Test
    void ordersEqualDueDatesByIdAndSupportsOneSidedRanges() {
        Task future = save(project, "미래", "A", TODAY.plusDays(1), TaskStatus.TODO);
        Task first = save(project, "오늘 첫째", "A", TODAY, TaskStatus.TODO);
        Task second = save(project, "오늘 둘째", "A", TODAY, TaskStatus.TODO);
        tasks.flush();

        assertThat(tasks.search(project.getId(), null, null, null, TODAY, null, TODAY, TaskStatus.DONE))
                .extracting(Task::getId).containsExactly(first.getId(), second.getId());
        assertThat(tasks.search(project.getId(), null, null, TODAY.plusDays(1), null, null, TODAY, TaskStatus.DONE))
                .extracting(Task::getId).containsExactly(future.getId());
    }

    @Test
    void excludesUnmatchedAndForeignTasksFromDetailLookup() {
        Task task = save(project, "본인 소속", "A", TODAY, TaskStatus.TODO);
        tasks.flush();

        assertThat(tasks.findByIdAndProject_Id(task.getId(), other.getId())).isEmpty();
        assertThat(tasks.findByIdAndProject_Id(task.getId(), project.getId())).isPresent();
        assertThat(tasks.search(project.getId(), "없는 담당자", null, null, null, null, TODAY, TaskStatus.DONE))
                .isEmpty();
    }

    private Task save(Project owner, String title, String assignee, LocalDate dueDate, TaskStatus status) {
        return tasks.save(Task.create(owner, title, "", assignee, dueDate, status, NOW));
    }
}
