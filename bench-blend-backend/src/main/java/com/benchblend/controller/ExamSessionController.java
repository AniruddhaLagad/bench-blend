package com.benchblend.controller;

import com.benchblend.dto.ApiResponse;
import com.benchblend.dto.ExamSessionRequest;
import com.benchblend.dto.ExamSessionResponse;
import com.benchblend.service.ExamSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ExamSessionController {

    private final ExamSessionService examSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExamSessionResponse>> createSession(
            @Valid @RequestBody ExamSessionRequest request) {
        ExamSessionResponse response = examSessionService.createSession(request);
        return ResponseEntity.ok(ApiResponse.success("Session created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamSessionResponse>>> getAllSessions() {
        List<ExamSessionResponse> sessions = examSessionService.getAllActiveSessions();
        return ResponseEntity.ok(ApiResponse.success("Sessions fetched successfully", sessions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamSessionResponse>> getSessionById(@PathVariable Long id) {
        ExamSessionResponse session = examSessionService.getSessionById(id);
        return ResponseEntity.ok(ApiResponse.success("Session fetched successfully", session));
    }
}