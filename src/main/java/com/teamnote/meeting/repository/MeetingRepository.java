package com.teamnote.meeting.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.teamnote.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    boolean existsByProject_Id(Long projectId);

    Optional<Meeting> findByIdAndProject_Id(Long meetingId, Long projectId);

    @Query("""
            SELECT m FROM Meeting m
            WHERE m.project.id = :projectId
              AND (:from IS NULL OR m.endAt > :from)
              AND (:to IS NULL OR m.startAt < :to)
            ORDER BY m.startAt ASC, m.id ASC
            """)
    List<Meeting> search(
            @Param("projectId") Long projectId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
