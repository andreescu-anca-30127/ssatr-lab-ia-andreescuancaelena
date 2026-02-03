package com.ssatr.Project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_token")
@Data
public class QrToken {

    @Id
    private String id;

    @Column(name = "token", nullable = false, unique = true, length = 2048)
    private String token;

    @Column(name = "survey_id", nullable = false)
    private String surveyId;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}
