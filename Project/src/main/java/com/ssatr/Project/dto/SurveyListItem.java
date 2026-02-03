package com.ssatr.Project.dto;

import java.time.OffsetDateTime;

public record SurveyListItem(
        String id,
        OffsetDateTime feedbackStart,
        OffsetDateTime feedbackEnd
) {}
