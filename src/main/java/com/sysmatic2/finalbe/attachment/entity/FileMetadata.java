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

    @Column(name = "display_name")
    private String displayName; // 유저에게 보여질 파일 이름

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_category", nullable = false)
    private String fileCategory; // 파일 유형: profile, account, icon, proposal

    @Column(name = "file_category_item_id")
    private String fileCategoryItemId;

    @Column(name = "member_id", nullable = false)
    private String uploaderId;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // Soft Delete 여부

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "description")
    private String description;


    public void clearMetadata() {
        this.displayName = null;
        this.fileName = null;
        this.filePath = null;
        this.fileSize = null;
        this.contentType = null;
        this.description = null;
    }
}
