package com.toeicify.toeic.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by hungpham on 8/6/2025
 */
public interface QuestionJsonProjection {
    @JsonProperty("fn_get_exam_questions_by_parts")
    String getFnGetExamQuestionsByParts();

    @JsonProperty("fn_get_exam_questions_by_exam")
    String getFnGetExamQuestionsByExam();
}

