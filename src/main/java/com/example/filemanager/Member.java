package com.example.filemanager;

import jakarta.persistence.*;

@Entity
@Table(
    name = "member",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_member_username",
        columnNames = "username"
    )
)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    public Member() {}

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
