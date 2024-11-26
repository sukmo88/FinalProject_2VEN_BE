package com.sysmatic2.finalbe.attachment.dto;


import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDto {
    private Long id;
    private String displayName; // 유저에게 보여질 파일 이름
    private String fileName; // 저장된 파일 이름(UUID)
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String fileCategory; // profile, icon, proposal, account
    private String fileCategoryItemId;
    private String uploaderId;
    // private boolean isDeleted;  // Soft Delete 여부)
    //private LocalDateTime deletedAt;
    private LocalDateTime uploadedAt;
    //private String description;

    public static FileMetadataDto fromEntity(FileMetadata metadata) {
        FileMetadataDto dto = new FileMetadataDto();
        dto.setId(metadata.getId());
        dto.setDisplayName(metadata.getDisplayName());
        dto.setFileName(metadata.getFileName());
        dto.setFileSize(metadata.getFileSize());
        dto.setFilePath(metadata.getFilePath());
        dto.setContentType(metadata.getContentType());
        dto.setFileCategory(metadata.getFileCategory());
        dto.setFileCategoryItemId(metadata.getFileCategoryItemId());
        dto.setUploaderId(metadata.getUploaderId());
        dto.setUploadedAt(LocalDateTime.now());

        return dto;
    }

    public static FileMetadata toEntity(FileMetadataDto dto) {
        FileMetadata metadata = new FileMetadata();
        metadata.setId(dto.getId());
        metadata.setDisplayName(dto.getDisplayName());
        metadata.setFileName(dto.getFileName());
        metadata.setFileSize(dto.getFileSize());
        metadata.setFilePath(dto.getFilePath());
        metadata.setContentType(dto.getContentType());
        metadata.setFileCategory(dto.getFileCategory());
        metadata.setFileCategoryItemId(dto.getFileCategoryItemId());
        metadata.setUploaderId(dto.getUploaderId());
        metadata.setUploadedAt(LocalDateTime.now());
        return metadata;
    }
}
