package com.teamnote.common.controller;

import com.teamnote.common.config.MarkdownConfiguration;
import com.teamnote.common.exception.GlobalExceptionHandler;
import com.teamnote.common.service.MarkdownService;
import com.teamnote.common.service.MarkdownServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContentPreviewControllerTest {

    private static final String PREVIEW_PATH = "/api/content/preview";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MarkdownConfiguration configuration = new MarkdownConfiguration();
        MarkdownService markdownService = new MarkdownServiceImpl(
                configuration.markdownParser(), configuration.markdownHtmlRenderer(), configuration.markdownHtmlCleaner()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(new ContentPreviewController(markdownService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsRenderedHtmlInCommonResponse() throws Exception {
        mockMvc.perform(post(PREVIEW_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"markdown":"# TEAMNOTE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.html").value("<h1>TEAMNOTE</h1>"))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"markdown\":null}"})
    void rejectsMissingOrNullMarkdown(String body) throws Exception {
        mockMvc.perform(post(PREVIEW_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.fields[0].field").value("markdown"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12.5", "true"})
    void rejectsNonStringMarkdown(String value) throws Exception {
        mockMvc.perform(post(PREVIEW_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"markdown\":" + value + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }
}
