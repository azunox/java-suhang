package com.teamnote.project.repository;

import java.util.List;
import java.util.Optional;

import com.teamnote.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            select p from Project p
            order by p.id asc
            """)
    List<Project> findAllProjects();

    @Query("""
            select p from Project p
            where p.id = :projectId
            """)
    Optional<Project> findProjectById(@Param("projectId") Long projectId);
}
