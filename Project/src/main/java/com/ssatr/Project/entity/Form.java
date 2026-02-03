package com.ssatr.Project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "form")
@Data
public class Form {

    @Id
    private String id;

    @Column(name = "survey_id")
    private String survey;

    @Column(name="satisfaction")
    private Integer satisfaction;

    @Column(name = "user_id")
    private String user;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
