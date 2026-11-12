package com.teamnote.task.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 10);
    private static final LocalDateTime NOW = DATE.atTime(14, 0);
    private static final String MARKDOWN = "  # 설명\n\n내용  ";

    @Test
    void normalizesNamesAndPreservesMarkdownAndPastDueDate() {
        Task task = create(TaskStatus.TODO);

        assertThat(task.getTitle()).isEqualTo("업무");
        assertThat(task.getAssigneeName()).isEqualTo("담당자");
        assertThat(task.getDescriptionMarkdown()).isEqualTo(MARKDOWN);
        assertThat(task.getDueDate()).isEqualTo(DATE.minusDays(1));
        assertThat(task.getCompletedAt()).isNull();
    }

    @Test
    void recordsCompletionWhenCreatedDone() {
        assertThat(create(TaskStatus.DONE).getCompletedAt()).isEqualTo(NOW);
    }

    @Test
    void preservesClearsAndRecordsCompletionAcrossTransitions() {
        Task task = create(TaskStatus.IN_PROGRESS);
        task.update("업무", MARKDOWN, "담당자", DATE, TaskStatus.DONE, NOW.plusHours(1));
        assertThat(task.getCompletedAt()).isEqualTo(NOW.plusHours(1));
        task.update("변경 제목", MARKDOWN, "담당자", DATE, TaskStatus.DONE, NOW.plusHours(2));
        assertThat(task.getCompletedAt()).isEqualTo(NOW.plusHours(1));
        task.update("변경 제목", MARKDOWN, "담당자", DATE, TaskStatus.TODO, NOW.plusHours(3));
        assertThat(task.getCompletedAt()).isNull();
        task.update("변경 제목", MARKDOWN, "담당자", DATE, TaskStatus.DONE, NOW.plusHours(4));
        assertThat(task.getCompletedAt()).isEqualTo(NOW.plusHours(4));
    }

    @Test
    void invalidUpdateLeavesOriginalStateIntact() {
        Task task = create(TaskStatus.DONE);

        assertThatThrownBy(() -> task.update("새 제목", "변경", " ", DATE,
                TaskStatus.TODO, NOW.plusHours(1))).isInstanceOf(InvalidRequestException.class);

        assertThat(task.getTitle()).isEqualTo("업무");
        assertThat(task.getDescriptionMarkdown()).isEqualTo(MARKDOWN);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(task.getCompletedAt()).isEqualTo(NOW);
    }

    private Task create(TaskStatus status) {
        Project project = Project.create("프로젝트", "", DATE, DATE, ProjectStatus.PLANNED);
        return Task.create(project, "  업무  ", MARKDOWN, "  담당자  ", DATE.minusDays(1), status, NOW);
    }
}
