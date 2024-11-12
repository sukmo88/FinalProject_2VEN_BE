package com.sysmatic2.finalbe.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "file")
@Getter
@Setter
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    //@ManyToOne
    //@JoinColumn(name = "member_id", nullable = false) // 'member_id' 컬럼을 외래 키로 사용
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "related_entity", nullable = false)
    private String relatedEntity;

    @Column(name = "related_entity_id", nullable = false)
    private Long relatedEntityId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description")
    private String description;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

}
