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
    // private String fileName; // 저장된 파일 이름(UUID)
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


    public FileMetadataDto(FileMetadata fileMetadata) {
        this.id = fileMetadata.getId();
        this.displayName = fileMetadata.getDisplayName();
        this.fileSize = fileMetadata.getFileSize();
        this.filePath = fileMetadata.getFilePath();
        this.contentType = fileMetadata.getContentType();
        this.uploaderId = fileMetadata.getUploaderId();
        this.fileCategory = fileMetadata.getFileCategory();
        this.fileCategoryItemId = fileMetadata.getFileCategoryItemId();
        this.uploadedAt = fileMetadata.getUploadedAt();
    }
}
