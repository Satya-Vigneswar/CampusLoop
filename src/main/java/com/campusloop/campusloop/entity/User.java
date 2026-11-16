package com.campusloop.campusloop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "campus_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 150)
    private String collegeName;

    @Column(nullable = false, length = 100)
    private String department;

    protected User() {
    }

    public User(String fullName, String email, String passwordHash, String collegeName, String department) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.collegeName = collegeName;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public String getDepartment() {
        return department;
    }
}
