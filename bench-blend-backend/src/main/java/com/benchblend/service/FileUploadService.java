package com.benchblend.service;

import com.benchblend.dto.ExamScheduleResponse;
import com.benchblend.dto.RoomBlockResponse;
import com.benchblend.model.ExamSchedule;
import com.benchblend.model.ExamSession;
import com.benchblend.model.RoomBlock;
import com.benchblend.repository.ExamScheduleRepository;
import com.benchblend.repository.RoomBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileParserService fileParserService;
    private final ExamSessionService examSessionService;
    private final ExamScheduleRepository examScheduleRepository;
    private final RoomBlockRepository roomBlockRepository;

    @Transactional
    public List<ExamScheduleResponse> uploadExamSchedule(Long sessionId,
                                                          LocalDate examDate,
                                                          MultipartFile file) throws Exception {
        ExamSession session = examSessionService.getSessionEntityById(sessionId);
        examScheduleRepository.deleteBySessionIdAndExamDate(sessionId, examDate);
        List<ExamSchedule> schedules = fileParserService.parseExamSchedule(file, session, examDate);
        List<ExamSchedule> saved = examScheduleRepository.saveAll(schedules);
        return saved.stream().map(this::mapSchedule).toList();
    }

    @Transactional
    public List<RoomBlockResponse> uploadRoomSkeleton(Long sessionId,
                                                       LocalDate examDate,
                                                       MultipartFile file) throws Exception {
        ExamSession session = examSessionService.getSessionEntityById(sessionId);
        roomBlockRepository.deleteBySessionIdAndExamDate(sessionId, examDate);
        List<RoomBlock> blocks = fileParserService.parseRoomSkeleton(file, session, examDate);
        List<RoomBlock> saved = roomBlockRepository.saveAll(blocks);
        return saved.stream().map(this::mapBlock).toList();
    }

    public List<ExamScheduleResponse> getExamSchedule(Long sessionId, LocalDate examDate) {
        return examScheduleRepository.findBySessionIdAndExamDate(sessionId, examDate)
                .stream().map(this::mapSchedule).toList();
    }

    public List<RoomBlockResponse> getRoomBlocks(Long sessionId, LocalDate examDate) {
        return roomBlockRepository.findBySessionIdAndExamDateOrderByBlockNo(sessionId, examDate)
                .stream().map(this::mapBlock).toList();
    }

    private ExamScheduleResponse mapSchedule(ExamSchedule s) {
        return ExamScheduleResponse.builder()
                .id(s.getId())
                .examDate(s.getExamDate())
                .timeSlot(s.getTimeSlot())
                .subjectCode(s.getSubjectCode())
                .semester(s.getSemester())
                .subjectName(s.getSubjectName())
                .className(s.getClassName())
                .studentCount(s.getStudentCount())
                .version(s.getVersion())
                .build();
    }

    private RoomBlockResponse mapBlock(RoomBlock b) {
        return RoomBlockResponse.builder()
                .id(b.getId())
                .examDate(b.getExamDate())
                .blockNo(b.getBlockNo())
                .roomNo(b.getRoomNo())
                .strength(b.getStrength())
                .build();
    }
}