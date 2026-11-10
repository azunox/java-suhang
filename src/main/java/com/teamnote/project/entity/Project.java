package com.teamnote.project.entity;

import java.time.LocalDate;

import com.teamnote.common.exception.InvalidRequestException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Lob
    @Column(nullable = false)
    private String descriptionMarkdown;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProjectStatus status;

    protected Project() {
    }

    public static Project create(
            String name,
            String descriptionMarkdown,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status
    ) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("프로젝트 이름은 필수입니다.");
        }
        if (name.length() > 120) {
            throw new InvalidRequestException("프로젝트 이름은 120자 이하여야 합니다.");
        }
        if (descriptionMarkdown == null) {
            throw new InvalidRequestException("프로젝트 설명은 필수입니다.");
        }
        if (startDate == null || endDate == null) {
            throw new InvalidRequestException("프로젝트 시작일과 종료일은 필수입니다.");
        }
        if (status == null) {
            throw new InvalidRequestException("프로젝트 상태는 필수입니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidRequestException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        Project project = new Project();
        project.name = name.strip();
        project.descriptionMarkdown = descriptionMarkdown;
        project.startDate = startDate;
        project.endDate = endDate;
        project.status = status;
        return project;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescriptionMarkdown() {
        return descriptionMarkdown;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }
}
