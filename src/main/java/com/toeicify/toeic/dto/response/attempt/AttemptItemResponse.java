package com.toeicify.toeic.dto.response.attempt;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptItemResponse {
    private Long attemptId;
    private Boolean fullTest;
    private List<Integer> parts;     // [1,2,4,6] nếu luyện tập nhiều part
    private Integer correct;         // tổng đúng (sum(scorePart))
    private Integer total;           // tổng câu (sum(expectedQuestionCount))
    private Integer toeicScore;      // null nếu không phải full test
    private Instant startTime;
    private Instant endTime;
    private Long durationSeconds;    // tiện render thời gian
}
