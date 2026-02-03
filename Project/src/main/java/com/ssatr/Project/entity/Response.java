package com.ssatr.Project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "response")
@Data
public class Response {

    @Id
    private String id;

    @Column(name = "question_id")
    private String questionId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "response")
    private String response;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
