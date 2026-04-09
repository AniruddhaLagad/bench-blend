package com.benchblend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_schedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession session;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(nullable = false, length = 50)
    private String timeSlot;

    @Column(nullable = false, length = 50)
    private String subjectCode;

    @Column(nullable = false, length = 10)
    private String semester;

    @Column(nullable = false, length = 255)
    private String subjectName;

    @Column(nullable = false, length = 100)
    private String className;

    @Column(nullable = false)
    private Integer studentCount;

    @Column(length = 20)
    private String version;

    @CreationTimestamp
    private LocalDateTime createdAt;
}