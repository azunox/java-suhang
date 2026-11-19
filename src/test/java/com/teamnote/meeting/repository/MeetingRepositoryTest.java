package com.teamnote.meeting.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.teamnote.meeting.entity.Meeting;
import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MeetingRepositoryTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 19);
    private final MeetingRepository meetings;
    private final ProjectRepository projects;
    private Project project;
    private Project otherProject;

    @Autowired
    MeetingRepositoryTest(MeetingRepository meetings, ProjectRepository projects) {
        this.meetings = meetings;
        this.projects = projects;
    }

    @BeforeEach
    void setUp() {
        project = projects.save(Project.create("회의 검색", "", DATE, DATE, ProjectStatus.IN_PROGRESS));
        otherProject = projects.save(Project.create("다른 프로젝트", "", DATE, DATE, ProjectStatus.PLANNED));
        save(project, "끝 경계", 9, 0, 10, 0);
        save(project, "시작 걸침", 9, 30, 10, 30);
        save(project, "동일 구간", 10, 0, 11, 0);
        save(project, "종료 걸침", 10, 30, 11, 30);
        save(project, "전체 포함", 9, 0, 12, 0);
        save(project, "시작 경계", 11, 0, 12, 0);
        save(otherProject, "다른 소속", 10, 0, 11, 0);
        meetings.flush();
    }

    @Test
    void searchesOverlappingIntervalsWithExclusiveBoundariesAndStableOrder() {
        assertThat(meetings.search(project.getId(), at(10, 0), at(11, 0)))
                .extracting(Meeting::getTitle)
                .containsExactly("전체 포함", "시작 걸침", "동일 구간", "종료 걸침");
    }

    @Test
    void supportsFromOnly() {
        assertThat(meetings.search(project.getId(), at(11, 30), null))
                .extracting(Meeting::getTitle).containsExactly("전체 포함", "시작 경계");
    }

    @Test
    void supportsToOnly() {
        assertThat(meetings.search(project.getId(), null, at(9, 30)))
                .extracting(Meeting::getTitle).containsExactly("끝 경계", "전체 포함");
    }

    @Test
    void supportsNoBoundsAndExcludesOtherProject() {
        assertThat(meetings.search(project.getId(), null, null))
                .extracting(Meeting::getTitle)
                .containsExactly("끝 경계", "전체 포함", "시작 걸침", "동일 구간", "종료 걸침", "시작 경계");
    }

    @Test
    void returnsEmptyWhenNothingOverlaps() {
        assertThat(meetings.search(project.getId(), at(12, 0), null)).isEmpty();
    }

    @Test
    void restrictsDetailLookupToOwningProject() {
        Long id = meetings.search(project.getId(), null, null).getFirst().getId();
        assertThat(meetings.findByIdAndProject_Id(id, project.getId())).isPresent();
        assertThat(meetings.findByIdAndProject_Id(id, otherProject.getId())).isEmpty();
    }

    private void save(Project owner, String title, int startHour, int startMinute, int endHour, int endMinute) {
        meetings.save(Meeting.create(owner, title, at(startHour, startMinute), at(endHour, endMinute), ""));
    }

    private LocalDateTime at(int hour, int minute) {
        return DATE.atTime(hour, minute);
    }
}
