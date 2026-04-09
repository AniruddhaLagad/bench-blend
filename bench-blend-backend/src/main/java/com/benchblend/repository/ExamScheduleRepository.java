package com.benchblend.repository;

import com.benchblend.model.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    List<ExamSchedule> findBySessionIdAndExamDate(Long sessionId, LocalDate examDate);
    void deleteBySessionIdAndExamDate(Long sessionId, LocalDate examDate);
}