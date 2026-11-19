package com.teamnote.task.integration;

import java.time.LocalDate;

import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import com.teamnote.project.repository.ProjectRepository;
import com.teamnote.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TaskApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ProjectRepository projects;
    @Autowired
    private TaskRepository tasks;

    private MockMvc mvc;
    private String base;
    private Long otherId;
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void prepare() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        LocalDate date = LocalDate.of(2026, 11, 19);
        Project project = projects.save(Project.create("업무 검증", "", date, date, ProjectStatus.IN_PROGRESS));
        otherId = projects.save(Project.create("다른 소속", "", date, date, ProjectStatus.PLANNED)).getId();
        base = "/api/projects/" + project.getId() + "/tasks";
    }

    @Test
    void persistsCrudAndEnforcesOwnershipWithRealServices() throws Exception {
        String body = request("# 업무", "TODO");
        String created = mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.descriptionHtml").value("<h1>업무</h1>"))
                .andExpect(jsonPath("$.data.completedAt").isEmpty())
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).path("data").path("id").asLong();
        String path = base + "/" + id;
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content(request("# 완료", "DONE")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.completedAt").isNotEmpty());
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.descriptionMarkdown").value("# 완료"));
        String foreign = "/api/projects/" + otherId + "/tasks/" + id;
        mvc.perform(get(foreign)).andExpect(status().isNotFound());
        mvc.perform(put(foreign).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
        mvc.perform(delete(foreign)).andExpect(status().isNotFound());
        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.completedAt").isEmpty());
        mvc.perform(delete(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        tasks.flush();
        assertThat(tasks.findById(id)).isEmpty();
        mvc.perform(get(path)).andExpect(status().isNotFound());
    }

    @Test
    void convertsTablesAndCodeWhileKeepingRawMarkdownAndBlockingUnsafeHtml() throws Exception {
        String markdown = "| 항목 | 내용 |\n| --- | --- |\n| 담당 | A |\n\n```java\nint count = 1;\n```\n\n"
                + "<script>alert(1)</script>\n\n[위험](javascript:alert(1))\n\n[정상](https://example.com)";
        String result = mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON)
                        .content(request(markdown, "TODO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.descriptionMarkdown").value(markdown))
                .andExpect(jsonPath("$.data.descriptionHtml").value(containsString("<table>")))
                .andExpect(jsonPath("$.data.descriptionHtml").value(containsString("<code>")))
                .andExpect(jsonPath("$.data.descriptionHtml").value(not(containsString("<script>"))))
                .andExpect(jsonPath("$.data.descriptionHtml").value(not(containsString("javascript:"))))
                .andExpect(jsonPath("$.data.descriptionHtml").value(containsString("https://example.com")))
                .andReturn().getResponse().getContentAsString();
        JsonNode data = json.readTree(result).path("data");
        mvc.perform(get(base + "/" + data.path("id").asLong())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.descriptionHtml").value(data.path("descriptionHtml").asText()));
    }

    @Test
    void rejectsInvalidSearchDatesAndMissingProjectsAndAcceptsEmptyMarkdown() throws Exception {
        mvc.perform(get(base).param("dueDateFrom", "2026-11-20").param("dueDateTo", "2026-11-19"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(base).param("dueDateFrom", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/projects/9223372036854775807/tasks")).andExpect(status().isNotFound());
        mvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content(request("", "TODO")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.descriptionHtml").value(""));
    }

    private String request(String markdown, String status) throws Exception {
        return "{\"title\":\"업무\",\"descriptionMarkdown\":" + json.writeValueAsString(markdown)
                + ",\"assigneeName\":\"팀원 A\",\"dueDate\":\"2026-11-19\",\"status\":\"" + status + "\"}";
    }
}
