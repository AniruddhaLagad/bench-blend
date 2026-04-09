package com.benchblend.repository;

import com.benchblend.model.SeatingArrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SeatingArrangementRepository extends JpaRepository<SeatingArrangement, Long> {
    List<SeatingArrangement> findBySessionIdAndExamDateOrderByBlockNo(Long sessionId, LocalDate examDate);
    void deleteBySessionIdAndExamDate(Long sessionId, LocalDate examDate);
}