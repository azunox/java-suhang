package com.teamnote.task.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.teamnote.task.entity.Task;
import com.teamnote.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndProject_Id(Long taskId, Long projectId);

    @Query("""
            select t from Task t
            where t.project.id = :projectId
              and (:assigneeName is null or t.assigneeName = :assigneeName)
              and (:status is null or t.status = :status)
              and (:dueDateFrom is null or t.dueDate >= :dueDateFrom)
              and (:dueDateTo is null or t.dueDate <= :dueDateTo)
              and (:overdue is null
                   or (:overdue = true and t.dueDate < :today and t.status <> :doneStatus)
                   or (:overdue = false and (t.dueDate >= :today or t.status = :doneStatus)))
            order by t.dueDate asc, t.id asc
            """)
    List<Task> search(
            @Param("projectId") Long projectId,
            @Param("assigneeName") String assigneeName,
            @Param("status") TaskStatus status,
            @Param("dueDateFrom") LocalDate dueDateFrom,
            @Param("dueDateTo") LocalDate dueDateTo,
            @Param("overdue") Boolean overdue,
            @Param("today") LocalDate today,
            @Param("doneStatus") TaskStatus doneStatus
    );
}
