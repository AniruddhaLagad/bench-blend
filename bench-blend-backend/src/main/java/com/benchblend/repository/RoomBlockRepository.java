package com.benchblend.repository;

import com.benchblend.model.RoomBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface RoomBlockRepository extends JpaRepository<RoomBlock, Long> {
    List<RoomBlock> findBySessionIdAndExamDateOrderByBlockNo(Long sessionId, LocalDate examDate);
    void deleteBySessionIdAndExamDate(Long sessionId, LocalDate examDate);
}