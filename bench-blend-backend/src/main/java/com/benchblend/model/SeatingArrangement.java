package com.benchblend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "seating_arrangements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatingArrangement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession session;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(nullable = false)
    private Integer blockNo;

    @Column(nullable = false, length = 20)
    private String roomNo;

    @Column(nullable = false)
    private Integer strength;

    @Column(length = 100)
    private String className;

    @Column(length = 50)
    private String timeSlot;

    @Column(length = 50)
    private String subjectCode;

    @Column(length = 10)
    private String semester;

    @Column(length = 255)
    private String subjectName;

    @Column(nullable = false)
    private Integer benchesUsed;

    @Column(nullable = false, length = 10)
    private String seatingMode;

    @Column(length = 5)
    private String side;

    @CreationTimestamp
    private LocalDateTime createdAt;
}