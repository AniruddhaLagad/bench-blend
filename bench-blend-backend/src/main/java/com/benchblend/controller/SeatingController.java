package com.benchblend.controller;

import com.benchblend.dto.ApiResponse;
import com.benchblend.dto.GenerateSeatingRequest;
import com.benchblend.dto.SeatingArrangementResponse;
import com.benchblend.service.SeatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/seating")
@RequiredArgsConstructor
public class SeatingController {

    private final SeatingService seatingService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<SeatingArrangementResponse>>> generate(
            @Valid @RequestBody GenerateSeatingRequest request) {
        List<SeatingArrangementResponse> result = seatingService.generateSeating(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Seating arrangement generated successfully. " + result.size() + " block assignments created.",
                result));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<List<SeatingArrangementResponse>>> getSeating(
            @PathVariable Long sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate) {
        List<SeatingArrangementResponse> result = seatingService.getSeating(sessionId, examDate);
        return ResponseEntity.ok(ApiResponse.success("Seating arrangement fetched", result));
    }
}