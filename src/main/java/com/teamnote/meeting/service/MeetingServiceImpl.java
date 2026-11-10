package com.teamnote.meeting.service;

import java.util.List;

import com.teamnote.common.exception.ResourceNotFoundException;
import com.teamnote.meeting.dto.MeetingCreateRequest;
import com.teamnote.meeting.dto.MeetingResponse;
import com.teamnote.meeting.entity.Meeting;
import com.teamnote.meeting.repository.MeetingRepository;
import com.teamnote.project.entity.Project;
import com.teamnote.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;

    public MeetingServiceImpl(MeetingRepository meetingRepository, ProjectRepository projectRepository) {
        this.meetingRepository = meetingRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public MeetingResponse create(Long projectId, MeetingCreateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));
        Meeting meeting = Meeting.create(
                project, request.title(), request.startAt(), request.endAt(), request.minutesMarkdown()
        );
        return toResponse(meetingRepository.save(meeting));
    }

    @Override
    public List<MeetingResponse> findAll(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("프로젝트를 찾을 수 없습니다.");
        }
        return meetingRepository.findAllByProject_IdOrderByStartAtAscIdAsc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MeetingResponse findById(Long projectId, Long meetingId) {
        Meeting meeting = meetingRepository.findByIdAndProject_Id(meetingId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("회의를 찾을 수 없습니다."));
        return toResponse(meeting);
    }

    private MeetingResponse toResponse(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getProject().getId(),
                meeting.getTitle(),
                meeting.getStartAt(),
                meeting.getEndAt(),
                meeting.getMinutesMarkdown()
        );
    }
}
