package com.teamnote.meeting.controller;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.teamnote.common.controller.SearchRequestBinding;
import com.teamnote.common.response.ApiResponse;
import com.teamnote.meeting.dto.MeetingCreateRequest;
import com.teamnote.meeting.dto.MeetingDateTimeParser;
import com.teamnote.meeting.dto.MeetingResponse;
import com.teamnote.meeting.dto.MeetingSearchRequest;
import com.teamnote.meeting.dto.MeetingUpdateRequest;
import com.teamnote.meeting.service.MeetingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @InitBinder("meetingSearchRequest")
    void bindSearchDateTimes(WebDataBinder binder) {
        SearchRequestBinding.useQueryParameters(binder);
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                try {
                    setValue(MeetingDateTimeParser.parse(text));
                } catch (DateTimeParseException exception) {
                    throw new IllegalArgumentException("회의 시각은 yyyy-MM-dd'T'HH:mm:ss 형식이어야 합니다.", exception);
                }
            }
        });
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "회의 등록 성공", useReturnTypeSchema = true
    )
    public ResponseEntity<ApiResponse<MeetingResponse>> create(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @Valid @RequestBody MeetingCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(meetingService.create(projectId, request)));
    }

    @GetMapping
    public ApiResponse<List<MeetingResponse>> findAll(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @Valid @ModelAttribute @ParameterObject MeetingSearchRequest request
    ) {
        return ApiResponse.success(meetingService.findAll(projectId, request));
    }

    @GetMapping("/{meetingId}")
    public ApiResponse<MeetingResponse> findById(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @PathVariable @Positive(message = "회의 ID는 양수여야 합니다.") Long meetingId
    ) {
        return ApiResponse.success(meetingService.findById(projectId, meetingId));
    }

    @PutMapping("/{meetingId}")
    public ApiResponse<MeetingResponse> update(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @PathVariable @Positive(message = "회의 ID는 양수여야 합니다.") Long meetingId,
            @Valid @RequestBody MeetingUpdateRequest request
    ) {
        return ApiResponse.success(meetingService.update(projectId, meetingId, request));
    }

    @DeleteMapping("/{meetingId}")
    public ApiResponse<Void> delete(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @PathVariable @Positive(message = "회의 ID는 양수여야 합니다.") Long meetingId
    ) {
        meetingService.delete(projectId, meetingId);
        return ApiResponse.success(null);
    }
}
