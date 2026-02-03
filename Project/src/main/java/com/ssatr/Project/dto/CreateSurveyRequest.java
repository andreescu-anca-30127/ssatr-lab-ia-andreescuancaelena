package com.ssatr.Project.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateSurveyRequest(
        @NotNull OffsetDateTime feedbackStart,
        @NotNull OffsetDateTime feedbackEnd,
        @NotEmpty List<String> questions
) {}
