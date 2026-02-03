package com.ssatr.Project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    private String id;

    @Column(name = "isOrganizer")
    private Boolean isOrganizer;

    @Column(name = "password")
    private String password;

    @Column(name = "username")
    private String username;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
