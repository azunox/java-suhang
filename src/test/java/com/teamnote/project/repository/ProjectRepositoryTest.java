package com.teamnote.project.repository;

import java.time.LocalDate;

import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectRepositoryTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 19);
    private static final String ACTIVE_API = "팀 REST API";
    private static final String PLANNED_API = "API 복습";
    private static final String SPECIAL_NAME = "릴리스_50%!";
    private static final String SIMILAR_NAME = "릴리스X50Y!";

    private final ProjectRepository projectRepository;

    @Autowired
    ProjectRepositoryTest(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @BeforeEach
    void setUp() {
        projectRepository.save(Project.create(ACTIVE_API, "", DATE, DATE, ProjectStatus.IN_PROGRESS));
        projectRepository.save(Project.create(PLANNED_API, "", DATE, DATE, ProjectStatus.PLANNED));
        projectRepository.save(Project.create(SPECIAL_NAME, "", DATE, DATE, ProjectStatus.PLANNED));
        projectRepository.save(Project.create(SIMILAR_NAME, "", DATE, DATE, ProjectStatus.COMPLETED));
    }

    @Test
    void returnsAllProjectsInIdOrderWhenNoFiltersAreGiven() {
        assertThat(projectRepository.search(null, null))
                .extracting(Project::getName)
                .containsExactly(ACTIVE_API, PLANNED_API, SPECIAL_NAME, SIMILAR_NAME);
    }

    @Test
    void matchesPartOfNameIgnoringCase() {
        assertThat(projectRepository.search("%api%", null))
                .extracting(Project::getName)
                .containsExactly(ACTIVE_API, PLANNED_API);
    }

    @Test
    void filtersByStatus() {
        assertThat(projectRepository.search(null, ProjectStatus.PLANNED))
                .extracting(Project::getName)
                .containsExactly(PLANNED_API, SPECIAL_NAME);
    }

    @Test
    void requiresBothFiltersToMatch() {
        assertThat(projectRepository.search("%api%", ProjectStatus.PLANNED))
                .extracting(Project::getName)
                .containsExactly(PLANNED_API);
    }

    @Test
    void returnsEmptyListWhenNoProjectMatches() {
        assertThat(projectRepository.search("%api%", ProjectStatus.COMPLETED)).isEmpty();
    }

    @Test
    void matchesEscapedLikeCharactersLiterally() {
        assertThat(projectRepository.search("%!_50!%!!%", null))
                .extracting(Project::getName)
                .containsExactly(SPECIAL_NAME);
    }
}
