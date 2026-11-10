package com.teamnote.project.entity;

import java.time.LocalDate;
import java.util.stream.Stream;

import com.teamnote.common.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 11, 9);
    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 20);

    @Test
    void normalizesNameAndPreservesMarkdownWhitespace() {
        String markdown = "  # TEAMNOTE\n\n설명  \n";

        Project project = Project.create("  REST API 수행평가  ", markdown,
                START_DATE, END_DATE, ProjectStatus.IN_PROGRESS);

        assertThat(project.getName()).isEqualTo("REST API 수행평가");
        assertThat(project.getDescriptionMarkdown()).isEqualTo(markdown);
        assertThat(project.getStartDate()).isEqualTo(START_DATE);
        assertThat(project.getEndDate()).isEqualTo(END_DATE);
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    void acceptsSameDayAndEmptyMarkdownForPastCompletedProject() {
        LocalDate date = LocalDate.of(2020, 1, 1);

        Project project = Project.create("완료 프로젝트", "", date, date, ProjectStatus.COMPLETED);

        assertThat(project.getStartDate()).isEqualTo(date);
        assertThat(project.getEndDate()).isEqualTo(date);
        assertThat(project.getDescriptionMarkdown()).isEmpty();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void rejectsReversedDates() {
        assertThatThrownBy(() -> Project.create("프로젝트", "", END_DATE, START_DATE, ProjectStatus.PLANNED))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("시작일은 종료일보다 늦을 수 없습니다.");
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    void rejectsMissingRequiredFields(
            String name,
            String markdown,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status
    ) {
        assertThatThrownBy(() -> Project.create(name, markdown, startDate, endDate, status))
                .isInstanceOf(InvalidRequestException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void rejectsBlankOrTooLongName(String name) {
        assertThatThrownBy(() -> Project.create(name, "", START_DATE, END_DATE, ProjectStatus.PLANNED))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void acceptsNameAtLengthLimit() {
        String name = "가".repeat(120);

        Project project = Project.create(name, "", START_DATE, END_DATE, ProjectStatus.PLANNED);

        assertThat(project.getName()).isEqualTo(name);
    }

    static Stream<Arguments> missingRequiredFields() {
        return Stream.of(
                Arguments.of(null, "", START_DATE, END_DATE, ProjectStatus.PLANNED),
                Arguments.of("프로젝트", null, START_DATE, END_DATE, ProjectStatus.PLANNED),
                Arguments.of("프로젝트", "", null, END_DATE, ProjectStatus.PLANNED),
                Arguments.of("프로젝트", "", START_DATE, null, ProjectStatus.PLANNED),
                Arguments.of("프로젝트", "", START_DATE, END_DATE, null)
        );
    }

    static Stream<String> invalidNames() {
        return Stream.of("", " \t\n ", "가".repeat(121));
    }
}
