package com.teamnote.meeting.repository;

import java.util.List;
import java.util.Optional;

import com.teamnote.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    boolean existsByProject_Id(Long projectId);

    List<Meeting> findAllByProject_IdOrderByStartAtAscIdAsc(Long projectId);

    Optional<Meeting> findByIdAndProject_Id(Long meetingId, Long projectId);
}
