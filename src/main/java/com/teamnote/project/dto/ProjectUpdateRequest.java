package com.teamnote.project.dto;

import java.time.LocalDate;

import com.teamnote.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

public record ProjectUpdateRequest(
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        @Size(max = 120, message = "프로젝트 이름은 120자 이하여야 합니다.")
        String name,
        @NotNull(message = "프로젝트 설명은 필수입니다.")
        String descriptionMarkdown,
        @NotNull(message = "프로젝트 시작일은 필수입니다.")
        @JsonDeserialize(using = ProjectDateDeserializer.class)
        LocalDate startDate,
        @NotNull(message = "프로젝트 종료일은 필수입니다.")
        @JsonDeserialize(using = ProjectDateDeserializer.class)
        LocalDate endDate,
        @NotNull(message = "프로젝트 상태는 필수입니다.")
        @JsonDeserialize(using = ProjectStatusDeserializer.class)
        ProjectStatus status
) {
}
