package com.ssatr.Project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "question")
@Data
public class Question {

    @Id
    private String id;

    @Column(name = "text")
    private String text;

    @Column(name = "survey_id")
    private String surveyId;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
