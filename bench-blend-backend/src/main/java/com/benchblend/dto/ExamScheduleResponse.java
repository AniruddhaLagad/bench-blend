package com.benchblend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamScheduleResponse {
    private Long id;
    private LocalDate examDate;
    private String timeSlot;
    private String subjectCode;
    private String semester;
    private String subjectName;
    private String className;
    private Integer studentCount;
    private String version;
}