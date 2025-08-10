package com.toeicify.toeic.dto.response.exam;

import java.util.List;
import lombok.*;
/**
 * Created by hungpham on 8/9/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryResponse {
    private Long examId;
    private String examName;
    private String submitType;
    private List<Long> partsSubmitted;
}
