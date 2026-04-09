package com.benchblend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GenerateSeatingRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Seating mode is required (SINGLE or DOUBLE)")
    private String seatingMode;
}