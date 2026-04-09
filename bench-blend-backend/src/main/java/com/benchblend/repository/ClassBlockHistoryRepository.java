package com.benchblend.repository;

import com.benchblend.model.ClassBlockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ClassBlockHistoryRepository extends JpaRepository<ClassBlockHistory, Long> {
    List<ClassBlockHistory> findBySessionIdAndClassName(Long sessionId, String className);

    @Query("SELECT c.blockNo FROM ClassBlockHistory c WHERE c.session.id = :sessionId AND c.className = :className")
    Set<Integer> findBlockNosBySessionIdAndClassName(@Param("sessionId") Long sessionId,
                                                      @Param("className") String className);
    void deleteBySessionIdAndExamDate(Long sessionId, LocalDate examDate);
}