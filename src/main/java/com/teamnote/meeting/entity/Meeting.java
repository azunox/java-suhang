package com.teamnote.meeting.entity;

import java.time.LocalDateTime;

import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_meetings_project"))
    private Project project;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Lob
    @Column(nullable = false)
    private String minutesMarkdown;

    protected Meeting() {
    }

    public static Meeting create(
            Project project,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String minutesMarkdown
    ) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new InvalidRequestException("회의 시작 시각은 종료 시각보다 빨라야 합니다.");
        }

        Meeting meeting = new Meeting();
        meeting.project = project;
        meeting.title = title.strip();
        meeting.startAt = startAt;
        meeting.endAt = endAt;
        meeting.minutesMarkdown = minutesMarkdown;
        return meeting;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public String getMinutesMarkdown() {
        return minutesMarkdown;
    }
}
