package com.teamnote.meeting.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.teamnote.common.exception.InvalidRequestException;
import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.meeting.dto.MeetingCreateRequest;
import com.teamnote.meeting.dto.MeetingResponse;
import com.teamnote.meeting.entity.Meeting;
import com.teamnote.meeting.repository.MeetingRepository;
import com.teamnote.project.entity.Project;
import com.teamnote.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplTest {

    private static final LocalDateTime START_AT = LocalDateTime.of(2026, 11, 9, 23, 30);
    private static final LocalDateTime END_AT = START_AT.plusHours(1);

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private Project project;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    @Test
    void savesMeetingUnderProjectWithNormalizedTitleAndUnchangedMarkdown() {
        String markdown = "  ## 회의록\n\n메모  \n";
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(project.getId()).thenReturn(1L);
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MeetingResponse response = meetingService.create(
                1L, new MeetingCreateRequest("  개발 점검  ", START_AT, END_AT, markdown)
        );

        ArgumentCaptor<Meeting> savedMeeting = ArgumentCaptor.forClass(Meeting.class);
        verify(meetingRepository).save(savedMeeting.capture());
        assertThat(savedMeeting.getValue().getProject()).isSameAs(project);
        assertThat(response).isEqualTo(new MeetingResponse(null, 1L, "개발 점검", START_AT, END_AT, markdown));
    }

    @Test
    void rejectsReversedTimesWithoutSaving() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        MeetingCreateRequest request = new MeetingCreateRequest("개발 점검", END_AT, START_AT, "");

        assertThatThrownBy(() -> meetingService.create(1L, request))
                .isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(meetingRepository);
    }

    @Test
    void rejectsCreateWhenProjectDoesNotExist() {
        MeetingCreateRequest request = new MeetingCreateRequest("개발 점검", START_AT, END_AT, "");

        assertThatThrownBy(() -> meetingService.create(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");

        verifyNoInteractions(meetingRepository);
    }

    @Test
    void mapsOrderedProjectMeetingsToDtos() {
        Meeting first = Meeting.create(project, "첫 회의", START_AT, END_AT, "");
        Meeting second = Meeting.create(project, "둘째 회의", END_AT, END_AT.plusHours(1), "");
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(project.getId()).thenReturn(1L);
        when(meetingRepository.findAllByProject_IdOrderByStartAtAscIdAsc(1L)).thenReturn(List.of(first, second));

        assertThat(meetingService.findAll(1L))
                .extracting(MeetingResponse::title)
                .containsExactly("첫 회의", "둘째 회의");
    }
}
