package com.teamnote.project.repository;

import java.util.List;
import java.util.Optional;

import com.teamnote.project.entity.Project;
import com.teamnote.project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            select p from Project p
            where (:namePattern is null or lower(p.name) like lower(:namePattern) escape '!')
              and (:status is null or p.status = :status)
            order by p.id asc
            """)
    List<Project> search(@Param("namePattern") String namePattern, @Param("status") ProjectStatus status);

    @Query("""
            select p from Project p
            where p.id = :projectId
            """)
    Optional<Project> findProjectById(@Param("projectId") Long projectId);
}
