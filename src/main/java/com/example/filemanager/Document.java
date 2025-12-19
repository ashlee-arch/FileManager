package com.example.filemanager;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // 서버 디스크에 저장된 파일명(UUID_원본명)
    @Column(nullable = false)
    private String filename;

    // 사용자가 업로드한 원본 파일명(다운로드/목록 표시용)
    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String ownerUsername;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Document() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
