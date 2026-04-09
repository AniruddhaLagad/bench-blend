package com.benchblend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_block_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClassBlockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession session;

    @Column(nullable = false, length = 100)
    private String className;

    @Column(nullable = false, length = 50)
    private String subjectCode;

    @Column(nullable = false)
    private Integer blockNo;

    @Column(nullable = false, length = 20)
    private String roomNo;

    @Column(nullable = false)
    private LocalDate examDate;

    @CreationTimestamp
    private LocalDateTime createdAt;
}