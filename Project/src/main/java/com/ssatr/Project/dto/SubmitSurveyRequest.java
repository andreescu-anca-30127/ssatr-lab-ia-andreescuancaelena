package com.ssatr.Project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitSurveyRequest(
        @NotNull String surveyId,
        String userId, // null => anonim
        @Min(1) @Max(5) Integer satisfaction,
        @NotNull List<AnswerItem> answers
) {
    public record AnswerItem(
            @NotNull String questionId,
            @NotNull String response
    ) {}
}
