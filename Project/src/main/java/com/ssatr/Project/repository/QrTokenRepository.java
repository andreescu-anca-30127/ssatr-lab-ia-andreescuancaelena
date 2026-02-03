package com.ssatr.Project.repository;

import com.ssatr.Project.entity.QrToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QrToken, String> {

    Optional<QrToken> findTopBySurveyIdAndUsedAtIsNullAndExpiresAtAfterOrderByExpiresAtDesc(
            String surveyId,
            OffsetDateTime now
    );

    Optional<QrToken> findByToken(String token);
}
