package com.toeicify.toeic.dto.response.attempt;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamHistoryResponse {
    private Long examId;
    private String examName;
    private List<AttemptItemResponse> attempts;
}