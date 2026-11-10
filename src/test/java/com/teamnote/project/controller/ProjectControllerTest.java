package com.teamnote.project.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import com.teamnote.common.exception.GlobalExceptionHandler;
import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.project.dto.ProjectCreateRequest;
import com.teamnote.project.dto.ProjectResponse;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private static final String MARKDOWN = "# TEAMNOTE\n\n설명  \n";
    private static final String VALID_REQUEST = """
            {
              "name": "  REST API 수행평가  ",
              "descriptionMarkdown": "# TEAMNOTE\\n\\n설명  \\n",
              "startDate": "2026-11-09",
              "endDate": "2026-11-20",
              "status": "IN_PROGRESS"
            }
            """;

    @Mock
    private ProjectService projectService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProjectController(projectService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsProjectWithCreatedStatusAndUnifiedResponse() throws Exception {
        when(projectService.create(any(ProjectCreateRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("REST API 수행평가"))
                .andExpect(jsonPath("$.data.descriptionMarkdown").value(MARKDOWN))
                .andExpect(jsonPath("$.data.startDate").value("2026-11-09"))
                .andExpect(jsonPath("$.data.endDate").value("2026-11-20"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.descriptionHtml").doesNotExist());

        verify(projectService).create(new ProjectCreateRequest("  REST API 수행평가  ", MARKDOWN,
                LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 20), ProjectStatus.IN_PROGRESS));
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void rejectsBlankOrTooLongNameBeforeCallingService(String name) throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace("  REST API 수행평가  ", name)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.fields[0].field").value("name"));

        verifyNoInteractions(projectService);
    }

    @Test
    void rejectsMissingRequiredFieldsBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.fields[*].field").value(containsInAnyOrder(
                        "name", "descriptionMarkdown", "startDate", "endDate", "status")));

        verifyNoInteractions(projectService);
    }

    @ParameterizedTest
    @CsvSource({
            "IN_PROGRESS, UNKNOWN",
            "2026-11-09, 2026-02-30",
            "2026-11-20, 20-11-2026"
    })
    void rejectsInvalidEnumOrDate(String original, String invalid) throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace(original, invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(projectService);
    }

    @Test
    void mapsInvalidProjectPeriodToBadRequest() throws Exception {
        when(projectService.create(any(ProjectCreateRequest.class)))
                .thenThrow(new InvalidRequestException("시작일은 종료일보다 늦을 수 없습니다."));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace("2026-11-09", "2026-11-21")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("시작일은 종료일보다 늦을 수 없습니다."));
    }

    @Test
    void rejectsMalformedJsonBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(projectService);
    }

    @Test
    void returnsProjectList() throws Exception {
        when(projectService.findAll()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void returnsEmptyArrayWhenNoProjectsExist() throws Exception {
        when(projectService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void returnsExistingProject() throws Exception {
        when(projectService.findById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("REST API 수행평가"));
    }

    @Test
    void mapsMissingProjectToNotFound() throws Exception {
        when(projectService.findById(999L))
                .thenThrow(new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "not-a-number", "9223372036854775808"})
    void rejectsInvalidProjectIdBeforeCallingService(String projectId) throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(projectService);
    }

    static Stream<String> invalidNames() {
        return Stream.of("", "   ", "가".repeat(121));
    }

    private ProjectResponse response() {
        return new ProjectResponse(1L, "REST API 수행평가", MARKDOWN,
                LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 20), ProjectStatus.IN_PROGRESS);
    }
}
