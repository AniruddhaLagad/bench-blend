package com.benchblend.service;

import com.benchblend.algorithm.SeatingAlgorithm;
import com.benchblend.dto.GenerateSeatingRequest;
import com.benchblend.dto.SeatingArrangementResponse;
import com.benchblend.model.*;
import com.benchblend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatingService {

    private final SeatingAlgorithm seatingAlgorithm;
    private final ExamSessionService examSessionService;
    private final ExamScheduleRepository examScheduleRepository;
    private final RoomBlockRepository roomBlockRepository;
    private final SeatingArrangementRepository seatingArrangementRepository;
    private final ClassBlockHistoryRepository classBlockHistoryRepository;

    @Transactional
    public List<SeatingArrangementResponse> generateSeating(GenerateSeatingRequest request) {
        Long sessionId = request.getSessionId();
        LocalDate examDate = request.getExamDate();
        String mode = request.getSeatingMode().toUpperCase();

        ExamSession session = examSessionService.getSessionEntityById(sessionId);

        List<ExamSchedule> schedules = examScheduleRepository
                .findBySessionIdAndExamDate(sessionId, examDate);
        List<RoomBlock> blocks = roomBlockRepository
                .findBySessionIdAndExamDateOrderByBlockNo(sessionId, examDate);

        if (schedules.isEmpty()) throw new RuntimeException("No exam schedule found for this date");
        if (blocks.isEmpty()) throw new RuntimeException("No room blocks found for this date");

        seatingArrangementRepository.deleteBySessionIdAndExamDate(sessionId, examDate);
        classBlockHistoryRepository.deleteBySessionIdAndExamDate(sessionId, examDate);

        List<SeatingArrangement> arrangements = seatingAlgorithm.generate(
                schedules, blocks, session, examDate, mode);

        List<SeatingArrangement> saved = seatingArrangementRepository.saveAll(arrangements);

        saveBlockHistory(saved, session);

        Map<String, Integer> roomSideCounter = new HashMap<>();
        return saved.stream()
                .map(a -> mapToResponse(a, roomSideCounter))
                .toList();
    }

    public List<SeatingArrangementResponse> getSeating(Long sessionId, LocalDate examDate) {
        Map<String, Integer> roomSideCounter = new HashMap<>();
        return seatingArrangementRepository
                .findBySessionIdAndExamDateOrderByBlockNo(sessionId, examDate)
                .stream()
                .map(a -> mapToResponse(a, roomSideCounter))
                .toList();
    }

    private void saveBlockHistory(List<SeatingArrangement> arrangements, ExamSession session) {
        List<ClassBlockHistory> histories = arrangements.stream()
                .filter(a -> a.getClassName() != null)
                .map(a -> ClassBlockHistory.builder()
                        .session(session)
                        .className(a.getClassName())
                        .subjectCode(a.getSubjectCode())
                        .blockNo(a.getBlockNo())
                        .roomNo(a.getRoomNo())
                        .examDate(a.getExamDate())
                        .build())
                .toList();
        classBlockHistoryRepository.saveAll(histories);
    }

    private SeatingArrangementResponse mapToResponse(SeatingArrangement a, Map<String, Integer> roomSideCounter) {
        String benchRange = buildBenchRange(a, roomSideCounter);
        return SeatingArrangementResponse.builder()
                .id(a.getId())
                .examDate(a.getExamDate())
                .blockNo(a.getBlockNo())
                .roomNo(a.getRoomNo())
                .strength(a.getStrength())
                .className(a.getClassName())
                .timeSlot(a.getTimeSlot())
                .subjectCode(a.getSubjectCode())
                .semester(a.getSemester())
                .subjectName(a.getSubjectName())
                .benchesUsed(a.getBenchesUsed())
                .seatingMode(a.getSeatingMode())
                .side(a.getSide())
                .benchRange(benchRange)
                .build();
    }

    private String buildBenchRange(SeatingArrangement a, Map<String, Integer> roomSideCounter) {
        String roomCode = a.getRoomNo().replace("-", "");
        String side = a.getSide() != null ? a.getSide() : "L";
        String key = roomCode + "-" + side;

        int startBench = roomSideCounter.getOrDefault(key, 0) + 1;
        int endBench = startBench + a.getBenchesUsed() - 1;

        roomSideCounter.put(key, endBench);

        return String.format("%s%s%02d to %s%s%02d",
                roomCode, side, startBench, roomCode, side, endBench);
    }
}