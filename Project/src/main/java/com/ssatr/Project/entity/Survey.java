package com.ssatr.Project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey")
@Data
public class Survey {

    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name="feedback_start")
    private OffsetDateTime feedbackStart;

    @Column(name="feedback_end")
    private OffsetDateTime feedbackEnd;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
