package com.blize.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity(name = "user")
@Data
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @JsonIgnore
    @Column(unique = true, nullable = false, length = 180)
    private String email;

    @Column(unique = true, nullable = false, length = 180)
    private String username;

    @JsonIgnore
    @Column(nullable = false, length = 127)
    private String password;

    @Column(nullable = true, length = 64)
    private String firstName;

    @Column(nullable = true, length = 64)
    private String lastName;

    @Column(nullable = true, length = 256)
    private String image;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime lastSeen;

    @JsonIgnore
    @Column(columnDefinition = "json")
    private String roles;

}
