package com.teamnote.task.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tasks_project"))
    private Project project;

    @Column(nullable = false, length = 120)
    private String title;

    @Lob
    @Column(nullable = false)
    private String descriptionMarkdown;

    @Column(nullable = false, length = 80)
    private String assigneeName;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status;

    private LocalDateTime completedAt;

    protected Task() {
    }

    public static Task create(
            Project project, String title, String descriptionMarkdown, String assigneeName,
            LocalDate dueDate, TaskStatus status, LocalDateTime now
    ) {
        if (project == null) {
            throw new InvalidRequestException("프로젝트는 필수입니다.");
        }
        if (title == null || title.isBlank() || title.length() > 120) {
            throw new InvalidRequestException("업무 제목은 필수이며 120자 이하여야 합니다.");
        }
        if (assigneeName == null || assigneeName.isBlank() || assigneeName.length() > 80) {
            throw new InvalidRequestException("담당자 이름은 필수이며 80자 이하여야 합니다.");
        }
        if (descriptionMarkdown == null || dueDate == null || status == null || now == null) {
            throw new InvalidRequestException("업무 설명·마감일·상태·처리 시각은 필수입니다.");
        }

        Task task = new Task();
        task.project = project;
        task.title = title.strip();
        task.descriptionMarkdown = descriptionMarkdown;
        task.assigneeName = assigneeName.strip();
        task.dueDate = dueDate;
        task.status = status;
        task.completedAt = status == TaskStatus.DONE ? now : null;
        return task;
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

    public String getDescriptionMarkdown() {
        return descriptionMarkdown;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
