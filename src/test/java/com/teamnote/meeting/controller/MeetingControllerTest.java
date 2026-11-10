package com.teamnote.meeting.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.teamnote.common.exception.GlobalExceptionHandler;
import com.teamnote.meeting.entity.Meeting;
import com.teamnote.meeting.repository.MeetingRepository;
import com.teamnote.meeting.service.MeetingServiceImpl;
import com.teamnote.project.entity.Project;
import com.teamnote.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MeetingControllerTest {

    private static final String MEETINGS_PATH = "/api/projects/{projectId}/meetings";
    private static final String VALID_REQUEST = """
            {
              "title": "  개발 점검  ",
              "startAt": "2026-11-18T23:30:00",
              "endAt": "2026-11-19T00:30:00",
              "minutesMarkdown": "## 결정 사항\\n\\n기록  \\n"
            }
            """;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private Project project;

    @Mock
    private Meeting savedMeeting;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MeetingServiceImpl meetingService = new MeetingServiceImpl(meetingRepository, projectRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(new MeetingController(meetingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsOvernightMeetingAndReturnsUnifiedResponse() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(project.getId()).thenReturn(1L);
        when(savedMeeting.getId()).thenReturn(7L);
        when(savedMeeting.getProject()).thenReturn(project);
        when(savedMeeting.getTitle()).thenReturn("개발 점검");
        when(savedMeeting.getStartAt()).thenReturn(LocalDateTime.parse("2026-11-18T23:30:00"));
        when(savedMeeting.getEndAt()).thenReturn(LocalDateTime.parse("2026-11-19T00:30:00"));
        when(savedMeeting.getMinutesMarkdown()).thenReturn("## 결정 사항\n\n기록  \n");
        when(meetingRepository.save(any(Meeting.class))).thenReturn(savedMeeting);

        mockMvc.perform(post(MEETINGS_PATH, 1).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.title").value("개발 점검"))
                .andExpect(jsonPath("$.data.startAt").value("2026-11-18T23:30:00"))
                .andExpect(jsonPath("$.data.endAt").value("2026-11-19T00:30:00"))
                .andExpect(jsonPath("$.data.minutesMarkdown").value("## 결정 사항\n\n기록  \n"))
                .andExpect(jsonPath("$.data.project").doesNotExist());
    }

    @Test
    void returnsEmptyArrayForExistingProjectWithoutMeetings() throws Exception {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(meetingRepository.findAllByProject_IdOrderByStartAtAscIdAsc(1L)).thenReturn(List.of());

        mockMvc.perform(get(MEETINGS_PATH, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void returnsMeetingDetailsOnlyForItsProject() throws Exception {
        when(project.getId()).thenReturn(1L);
        when(savedMeeting.getId()).thenReturn(7L);
        when(savedMeeting.getProject()).thenReturn(project);
        when(meetingRepository.findByIdAndProject_Id(7L, 1L)).thenReturn(Optional.of(savedMeeting));

        mockMvc.perform(get(MEETINGS_PATH + "/{meetingId}", 1, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.projectId").value(1));

        mockMvc.perform(get(MEETINGS_PATH + "/{meetingId}", 2, 7))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void rejectsMissingProjectInsteadOfReturningAnEmptyList() throws Exception {
        mockMvc.perform(get(MEETINGS_PATH, 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));

        verifyNoInteractions(meetingRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 7",
            "1, -1",
            "1, invalid"
    })
    void rejectsInvalidPathIds(String projectId, String meetingId) throws Exception {
        mockMvc.perform(get(MEETINGS_PATH + "/{meetingId}", projectId, meetingId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(projectRepository, meetingRepository);
    }

    @Test
    void rejectsBlankTitleBeforeCheckingProject() throws Exception {
        mockMvc.perform(post(MEETINGS_PATH, 1).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace("  개발 점검  ", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(projectRepository, meetingRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-11-18T23:30:00Z", "2026-11-18T23:30:00+09:00", "2026-02-30T23:30:00"})
    void rejectsOffsetsAndInvalidCalendarDates(String startAt) throws Exception {
        mockMvc.perform(post(MEETINGS_PATH, 1).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace("2026-11-18T23:30:00", startAt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(projectRepository, meetingRepository);
    }

    @Test
    void rejectsDateTimeArraysBeforeCheckingProject() throws Exception {
        mockMvc.perform(post(MEETINGS_PATH, 1).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace("\"2026-11-18T23:30:00\"", "[2026,11,18,23,30,0]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(projectRepository, meetingRepository);
    }

    @Test
    void returnsBadRequestForEqualStartAndEndWithoutSaving() throws Exception {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        mockMvc.perform(post(MEETINGS_PATH, 1).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace("2026-11-19T00:30:00", "2026-11-18T23:30:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(meetingRepository);
    }
}
