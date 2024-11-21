package com.sysmatic2.finalbe.attachment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false)
    private String displayName; // 유저에게 보여질 파일 이름

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_category", nullable = false)
    private String fileCategory; // 파일 유형: PROFILE, DOCUMENT, ACCOUNT_VERIFICATION 등

    @Column(name = "member_id", nullable = false)
    private String uploaderId;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // Soft Delete 여부

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "description", nullable = true)
    private String description;
}
