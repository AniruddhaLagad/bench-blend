package com.benchblend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomBlockResponse {
    private Long id;
    private LocalDate examDate;
    private Integer blockNo;
    private String roomNo;
    private Integer strength;
}