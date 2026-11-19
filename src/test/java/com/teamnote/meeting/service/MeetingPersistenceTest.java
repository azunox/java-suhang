package com.teamnote.meeting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.teamnote.common.config.MarkdownConfiguration;
import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.common.service.MarkdownServiceImpl;
import com.teamnote.meeting.dto.MeetingCreateRequest;
import com.teamnote.meeting.dto.MeetingResponse;
import com.teamnote.meeting.dto.MeetingSearchRequest;
import com.teamnote.meeting.dto.MeetingUpdateRequest;
import com.teamnote.meeting.repository.MeetingRepository;
import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({MeetingServiceImpl.class, MarkdownServiceImpl.class, MarkdownConfiguration.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MeetingPersistenceTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 19, 23, 30);
    private final MeetingService service;
    private final MeetingRepository meetings;
    private final ProjectRepository projects;
    private Long projectId;
    private Long otherProjectId;
    private Long meetingId;

    @Autowired
    MeetingPersistenceTest(MeetingService service, MeetingRepository meetings, ProjectRepository projects) {
        this.service = service;
        this.meetings = meetings;
        this.projects = projects;
    }

    @BeforeEach
    void setUp() {
        LocalDate date = START.toLocalDate();
        projectId = projects.save(Project.create("저장 검증", "", date, date, ProjectStatus.PLANNED)).getId();
        otherProjectId = projects.save(Project.create("다른 소속", "", date, date, ProjectStatus.PLANNED)).getId();
        meetingId = service.create(projectId, new MeetingCreateRequest("기존 회의", START, START.plusHours(1),
                "# 회의록")).id();
    }

    @AfterEach
    void cleanUp() {
        if (meetingId != null && meetings.existsById(meetingId)) {
            meetings.deleteById(meetingId);
        }
        projects.deleteById(projectId);
        projects.deleteById(otherProjectId);
    }

    @Test
    void commitsValidUpdateAndPreservesDatabaseValuesAfterInvalidUpdate() {
        MeetingResponse updated = service.update(projectId, meetingId, new MeetingUpdateRequest("변경 회의",
                START.plusDays(1), START.plusDays(1).plusHours(2), "## 변경\n"));
        assertThat(service.findById(projectId, meetingId)).isEqualTo(updated);
        assertThat(updated.minutesHtml()).isEqualTo("<h2>변경</h2>");

        assertThatThrownBy(() -> service.update(projectId, meetingId,
                new MeetingUpdateRequest("실패 제목", START, START, "실패 원문")))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(service.findById(projectId, meetingId)).isEqualTo(updated);
    }

    @Test
    void preventsForeignProjectUpdatesAndDeletesWithoutChangingOriginal() {
        MeetingResponse original = service.findById(projectId, meetingId);
        assertThatThrownBy(() -> service.update(otherProjectId, meetingId,
                new MeetingUpdateRequest("침범", START, START.plusHours(1), "")))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(otherProjectId, meetingId))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.findById(projectId, meetingId)).isEqualTo(original);
    }

    @Test
    void deletesOnlyMeetingAndReturnsNotFoundOnRepeat() {
        service.delete(projectId, meetingId);
        assertThat(meetings.existsById(meetingId)).isFalse();
        assertThat(projects.existsById(projectId)).isTrue();
        assertThatThrownBy(() -> service.findById(projectId, meetingId))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(projectId, meetingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rendersOriginalMarkdownForCreateListDetailAndUpdateIncludingEmptyInput() {
        MeetingResponse original = service.findById(projectId, meetingId);
        assertThat(original.minutesMarkdown()).isEqualTo("# 회의록");
        assertThat(original.minutesHtml()).isEqualTo("<h1>회의록</h1>");
        assertThat(service.search(projectId, new MeetingSearchRequest(START, START.plusHours(1))))
                .containsExactly(original);
        MeetingResponse empty = service.update(projectId, meetingId,
                new MeetingUpdateRequest("빈 회의록", START, START.plusHours(1), ""));
        assertThat(empty.minutesHtml()).isEmpty();
        assertThat(service.findAll(projectId)).containsExactly(empty);
    }
}
