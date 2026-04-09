package com.benchblend.controller;

import com.benchblend.dto.ApiResponse;
import com.benchblend.dto.ExamScheduleResponse;
import com.benchblend.dto.RoomBlockResponse;
import com.benchblend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/schedule/{sessionId}")
    public ResponseEntity<ApiResponse<List<ExamScheduleResponse>>> uploadSchedule(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam("file") MultipartFile file) throws Exception {
        List<ExamScheduleResponse> response = fileUploadService.uploadExamSchedule(sessionId, examDate, file);
        return ResponseEntity.ok(ApiResponse.success(
                "Exam schedule uploaded successfully. " + response.size() + " subjects loaded.", response));
    }

    @PostMapping("/skeleton/{sessionId}")
    public ResponseEntity<ApiResponse<List<RoomBlockResponse>>> uploadSkeleton(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam("file") MultipartFile file) throws Exception {
        List<RoomBlockResponse> response = fileUploadService.uploadRoomSkeleton(sessionId, examDate, file);
        return ResponseEntity.ok(ApiResponse.success(
                "Room skeleton uploaded successfully. " + response.size() + " blocks loaded.", response));
    }

    @GetMapping("/schedule/{sessionId}")
    public ResponseEntity<ApiResponse<List<ExamScheduleResponse>>> getSchedule(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate) {
        List<ExamScheduleResponse> response = fileUploadService.getExamSchedule(sessionId, examDate);
        return ResponseEntity.ok(ApiResponse.success("Exam schedule fetched", response));
    }

    @GetMapping("/skeleton/{sessionId}")
    public ResponseEntity<ApiResponse<List<RoomBlockResponse>>> getSkeleton(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate) {
        List<RoomBlockResponse> response = fileUploadService.getRoomBlocks(sessionId, examDate);
        return ResponseEntity.ok(ApiResponse.success("Room blocks fetched", response));
    }
}