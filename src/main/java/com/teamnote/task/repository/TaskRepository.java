package com.teamnote.task.repository;

import java.util.List;
import java.util.Optional;

import com.teamnote.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndProject_Id(Long taskId, Long projectId);

    List<Task> findByProject_IdOrderByDueDateAscIdAsc(Long projectId);
}
