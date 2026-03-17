package com.finance.dto;

import com.finance.model.enums.InsightSeverity;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {

    private Long id;
    private String message;

    // INFO / WARNING / ALERT — controls color coding in frontend
    private InsightSeverity severity;

    private LocalDateTime createdAt;
}