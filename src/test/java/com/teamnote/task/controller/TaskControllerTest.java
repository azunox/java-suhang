package com.teamnote.task.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import com.teamnote.common.controller.SearchRequestBinding;
import com.teamnote.common.exception.GlobalExceptionHandler;
import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.task.dto.TaskCreateRequest;
import com.teamnote.task.dto.TaskResponse;
import com.teamnote.task.dto.TaskSearchRequest;
import com.teamnote.task.dto.TaskUpdateRequest;
import com.teamnote.task.entity.TaskStatus;
import com.teamnote.task.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                .setCustomArgumentResolvers(new SearchRequestBinding())
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

    @Test
    void returnsDetailAndSearchResults() throws Exception {
        when(service.findById(1L, 10L)).thenReturn(response());
        when(service.findAll(eq(1L), any(TaskSearchRequest.class))).thenReturn(List.of(response()));
        mvc.perform(get(BASE + "/10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("업무"));
        mvc.perform(get(BASE).queryParam("assigneeName", "담당자").queryParam("status", "TODO")
                        .queryParam("dueDateFrom", "2026-11-10").queryParam("overdue", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(10));
        verify(service).findAll(1L, new TaskSearchRequest("담당자", "TODO",
                LocalDate.of(2026, 11, 10), null, false));
    }

    @Test
    void updatesAndDeletesWithUnifiedResponse() throws Exception {
        when(service.update(eq(1L), eq(10L), any(TaskUpdateRequest.class))).thenReturn(response());
        mvc.perform(put(BASE + "/10").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(10));
        mvc.perform(delete(BASE + "/10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));
        verify(service).delete(1L, 10L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"title\":\" \"}",
            "{\"title\":\"업무\",\"descriptionMarkdown\":\"\",\"assigneeName\":\"담당자\",\"dueDate\":\"2026-02-30\",\"status\":\"TODO\"}",
            "{\"title\":\"업무\",\"descriptionMarkdown\":\"\",\"assigneeName\":\"담당자\",\"dueDate\":\"2026-11-10\",\"status\":\"UNKNOWN\"}"})
    void rejectsInvalidCreateAndUpdateBodies(String body) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        mvc.perform(put(BASE + "/10").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"status=UNKNOWN", "dueDateFrom=2026-02-30", "overdue=invalid"})
    void rejectsMalformedSearchValues(String query) throws Exception {
        mvc.perform(get(BASE + "?" + query)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @MethodSource("invalidSearchParameters")
    void rejectsPresentSearchParametersWithInvalidFormat(String parameter, String value) throws Exception {
        mvc.perform(get(BASE).queryParam(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.fields[0].field").value(parameter));
        verifyNoInteractions(service);
    }

    @Test
    void acceptsBothDateBoundsAndTrueOverdueFilter() throws Exception {
        TaskSearchRequest search = new TaskSearchRequest(null, null,
                LocalDate.of(2024, 2, 29), LocalDate.of(2026, 11, 20), true);
        when(service.findAll(1L, search)).thenReturn(List.of());

        mvc.perform(get(BASE).queryParam("dueDateFrom", "2024-02-29").queryParam("dueDateTo", "2026-11-20")
                        .queryParam("overdue", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service).findAll(1L, search);
    }

    @Test
    void searchIgnoresHeadersAndBinderPrefixes() throws Exception {
        mvc.perform(get(BASE).header("status", "TODO")
                        .queryParam("!dueDateFrom", "2026-11-19").queryParam("_overdue", "on"))
                .andExpect(status().isOk());

        verify(service).findAll(1L, new TaskSearchRequest(null, null, null, null, null));
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
        mvc.perform(put(BASE + "/10").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void returnsEmptySearchResultAndRejectsOversizedSearchAssignee() throws Exception {
        when(service.findAll(eq(1L), any(TaskSearchRequest.class))).thenReturn(List.of());
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        mvc.perform(get(BASE).queryParam("assigneeName", "가".repeat(81)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mapsReversedSearchRangeToBadRequest() throws Exception {
        when(service.findAll(eq(1L), any(TaskSearchRequest.class)))
                .thenThrow(new InvalidRequestException("검색 기간 오류"));
        mvc.perform(get(BASE).queryParam("dueDateFrom", "2026-11-20").queryParam("dueDateTo", "2026-11-10"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void mapsMissingProjectAndForeignTaskToNotFound() throws Exception {
        when(service.create(eq(1L), any(TaskCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));
        when(service.findById(1L, 10L)).thenThrow(new ResourceNotFoundException("업무를 찾을 수 없습니다."));
        when(service.update(eq(1L), eq(10L), any(TaskUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("업무를 찾을 수 없습니다."));
        doThrow(new ResourceNotFoundException("업무를 찾을 수 없습니다.")).when(service).delete(1L, 10L);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isNotFound());
        mvc.perform(get(BASE + "/10")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
        mvc.perform(put(BASE + "/10").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isNotFound());
        mvc.perform(delete(BASE + "/10")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void rejectsInvalidPathIds(String id) throws Exception {
        mvc.perform(get("/api/projects/" + id + "/tasks"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isBadRequest());
        mvc.perform(delete(BASE + "/" + id)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @MethodSource("invalidScalarBodies")
    void rejectsMalformedFieldsForCreateAndUpdate(String body) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        mvc.perform(put(BASE + "/10").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsEmptySearchStatusInsteadOfRemovingFilter(String value) throws Exception {
        mvc.perform(get(BASE).queryParam("status", value)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TODO", "IN_PROGRESS", "DONE"})
    void acceptsNamedStatusStringsForBodiesAndSearch(String value) throws Exception {
        when(service.create(eq(1L), any(TaskCreateRequest.class))).thenReturn(response());
        when(service.update(eq(1L), eq(10L), any(TaskUpdateRequest.class))).thenReturn(response());
        when(service.findAll(eq(1L), any(TaskSearchRequest.class))).thenReturn(List.of());
        String body = REQUEST.replace("TODO", value);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(put(BASE + "/10").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(get(BASE).queryParam("status", value)).andExpect(status().isOk());
        verify(service).create(1L, new TaskCreateRequest("업무", "", "담당자",
                LocalDate.of(2026, 11, 10), TaskStatus.valueOf(value)));
        verify(service).update(1L, 10L, new TaskUpdateRequest("업무", "", "담당자",
                LocalDate.of(2026, 11, 10), TaskStatus.valueOf(value)));
        verify(service).findAll(1L, new TaskSearchRequest(null, value, null, null, null));
    }

    private static Stream<String> invalidScalarBodies() {
        Stream<String> dates = Stream.of("[2026,11,10]", "20261110", "true", "{}", "\"\"", "\"2026-1-1\"", "null")
                .map(value -> REQUEST.replace("\"dueDate\":\"2026-11-10\"", "\"dueDate\":" + value));
        Stream<String> statuses = Stream.of("0", "1", "true", "[]", "\"0\"", "\"\"", "null")
                .map(value -> REQUEST.replace("\"status\":\"TODO\"", "\"status\":" + value));
        Stream<String> textFields = Stream.of(
                REQUEST.replace("\"업무\"", "123"),
                REQUEST.replace("\"descriptionMarkdown\":\"\"", "\"descriptionMarkdown\":true"),
                REQUEST.replace("\"담당자\"", "42.5")
        );
        return Stream.of(dates, statuses, textFields).flatMap(stream -> stream);
    }

    private static Stream<Arguments> invalidSearchParameters() {
        Stream<Arguments> dates = Stream.of("", " ", "2026-11-19+09:00", "2026-11-19Z", "2026-02-30", "+12026-11-19")
                .flatMap(value -> Stream.of(Arguments.of("dueDateFrom", value), Arguments.of("dueDateTo", value)));
        Stream<Arguments> overdue = Stream.of("", " ", "yes", "1", "TRUE", " true ")
                .map(value -> Arguments.of("overdue", value));
        return Stream.concat(dates, overdue);
    }

    private TaskResponse response() {
        return new TaskResponse(10L, 1L, "업무", "", "", "담당자",
                LocalDate.of(2026, 11, 10), TaskStatus.TODO, null);
    }
}
