package com.finance.model;

import com.finance.model.enums.InsightSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "insights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Longer message length — AI insights can be descriptive
    @Column(length = 600)
    private String message;

    @Enumerated(EnumType.STRING)
    private InsightSeverity severity;

    @CreationTimestamp
    private LocalDateTime createdAt;
}