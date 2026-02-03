package com.ssatr.Project.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record PublicSessionResponse(
        String surveyId,
        OffsetDateTime feedbackStart,
        OffsetDateTime feedbackEnd,
        List<QuestionDto> questions
) {
    public record QuestionDto(String id, String text) {}
}
