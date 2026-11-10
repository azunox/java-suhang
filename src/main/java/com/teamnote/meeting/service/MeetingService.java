package com.teamnote.meeting.service;

import java.util.List;

import com.teamnote.meeting.dto.MeetingCreateRequest;
import com.teamnote.meeting.dto.MeetingResponse;

public interface MeetingService {

    MeetingResponse create(Long projectId, MeetingCreateRequest request);

    List<MeetingResponse> findAll(Long projectId);

    MeetingResponse findById(Long projectId, Long meetingId);
}
