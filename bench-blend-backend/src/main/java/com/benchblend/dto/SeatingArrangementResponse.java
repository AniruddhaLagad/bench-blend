package com.benchblend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatingArrangementResponse {
    private Long id;
    private LocalDate examDate;
    private Integer blockNo;
    private String roomNo;
    private Integer strength;
    private String className;
    private String timeSlot;
    private String subjectCode;
    private String semester;
    private String subjectName;
    private Integer benchesUsed;
    private String seatingMode;
    private String side;
    private String benchRange;
}