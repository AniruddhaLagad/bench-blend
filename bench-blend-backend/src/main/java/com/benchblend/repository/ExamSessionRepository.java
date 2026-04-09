package com.benchblend.repository;

import com.benchblend.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByIsActiveTrue();
    Optional<ExamSession> findByIdAndIsActiveTrue(Long id);
}