package com.teamnote.task.controller;

import java.time.LocalDate;
import java.util.List;

import com.teamnote.common.exception.GlobalExceptionHandler;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.task.dto.TaskCreateRequest;
import com.teamnote.task.dto.TaskResponse;
import com.teamnote.task.entity.TaskStatus;
import com.teamnote.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private static final String BASE = "/api/projects/1/tasks";
    private static final String REQUEST = """
            {"title":"업무","descriptionMarkdown":"","assigneeName":"담당자",
             "dueDate":"2026-11-10","status":"TODO"}
            """;

    @Mock
    private TaskService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TaskController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsTaskWithUnifiedCreatedResponse() throws Exception {
        when(service.create(eq(1L), any(TaskCreateRequest.class))).thenReturn(response());
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.completedAt").value(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"title\":\" \"}",
            "{\"title\":\"업무\",\"descriptionMarkdown\":\"\",\"assigneeName\":\"담당자\",\"dueDate\":\"2026-02-30\",\"status\":\"TODO\"}",
            "{\"title\":\"업무\",\"descriptionMarkdown\":\"\",\"assigneeName\":\"담당자\",\"dueDate\":\"2026-11-10\",\"status\":\"UNKNOWN\"}"})
    void rejectsInvalidCreateBodies(String body) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "longTitle", "longAssignee"})
    void rejectsBlankTitleAndOversizedNamesWithOtherwiseValidBody(String scenario) throws Exception {
        String body = switch (scenario) {
            case "longTitle" -> REQUEST.replace("업무", "가".repeat(121));
            case "longAssignee" -> REQUEST.replace("담당자", "가".repeat(81));
            default -> REQUEST.replace("업무", " ");
        };
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @Test
    void mapsMissingProjectAndForeignTaskToNotFound() throws Exception {
        when(service.create(eq(1L), any(TaskCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));
        when(service.findById(1L, 10L)).thenThrow(new ResourceNotFoundException("업무를 찾을 수 없습니다."));
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isNotFound());
        mvc.perform(get(BASE + "/10")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void rejectsInvalidPathIds(String id) throws Exception {
        mvc.perform(get("/api/projects/" + id + "/tasks"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void returnsDetailAndProjectList() throws Exception {
        when(service.findById(1L, 10L)).thenReturn(response());
        when(service.findAll(1L)).thenReturn(List.of(response()));
        mvc.perform(get(BASE + "/10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("업무"));
        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    void returnsEmptyList() throws Exception {
        when(service.findAll(1L)).thenReturn(List.of());
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    private TaskResponse response() {
        return new TaskResponse(10L, 1L, "업무", "", "담당자",
                LocalDate.of(2026, 11, 10), TaskStatus.TODO, null);
    }
}
