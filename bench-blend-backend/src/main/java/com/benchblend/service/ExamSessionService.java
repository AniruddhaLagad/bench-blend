package com.benchblend.service;

import com.benchblend.dto.ExamSessionRequest;
import com.benchblend.dto.ExamSessionResponse;
import com.benchblend.model.ExamSession;
import com.benchblend.repository.ExamSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamSessionService {

    private final ExamSessionRepository examSessionRepository;

    public ExamSessionResponse createSession(ExamSessionRequest request) {
        ExamSession session = ExamSession.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();
        ExamSession saved = examSessionRepository.save(session);
        return mapToResponse(saved);
    }

    public List<ExamSessionResponse> getAllActiveSessions() {
        return examSessionRepository.findByIsActiveTrue()
                .stream().map(this::mapToResponse).toList();
    }

    public ExamSessionResponse getSessionById(Long id) {
        ExamSession session = examSessionRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
        return mapToResponse(session);
    }

    public ExamSession getSessionEntityById(Long id) {
        return examSessionRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
    }

    private ExamSessionResponse mapToResponse(ExamSession session) {
        return ExamSessionResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .isActive(session.getIsActive())
                .build();
    }
}