package com.teamnote.common.controller;

import com.teamnote.common.dto.ContentPreviewRequest;
import com.teamnote.common.dto.ContentPreviewResponse;
import com.teamnote.common.response.ApiResponse;
import com.teamnote.common.service.MarkdownService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/content")
public class ContentPreviewController {

    private final MarkdownService markdownService;

    public ContentPreviewController(MarkdownService markdownService) {
        this.markdownService = markdownService;
    }

    @PostMapping("/preview")
    public ApiResponse<ContentPreviewResponse> preview(@Valid @RequestBody ContentPreviewRequest request) {
        String html = markdownService.render(request.markdown());
        return ApiResponse.success(new ContentPreviewResponse(html));
    }
}
