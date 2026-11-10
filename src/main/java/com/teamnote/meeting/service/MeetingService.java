package com.teamnote.meeting.service;

import java.util.List;

import com.teamnote.meeting.dto.MeetingCreateRequest;
import com.teamnote.meeting.dto.MeetingResponse;
import com.teamnote.meeting.dto.MeetingUpdateRequest;

public interface MeetingService {

    MeetingResponse create(Long projectId, MeetingCreateRequest request);

    List<MeetingResponse> findAll(Long projectId);

    MeetingResponse findById(Long projectId, Long meetingId);

    MeetingResponse update(Long projectId, Long meetingId, MeetingUpdateRequest request);

    void delete(Long projectId, Long meetingId);
}
