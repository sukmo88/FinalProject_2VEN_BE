package com.sysmatic2.finalbe.attachment.repository;

import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;


public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFileName(String fileName);
    List<FileMetadata> findByFileCategoryAndUploaderId(String fileCategory, String uploaderId);
    Boolean existsByIdAndFileCategoryAndUploaderId(Long id, String fileCategory, String uploaderId);
    Optional<FileMetadata> findByFileNameAndUploaderIdAndFileCategory(String fileName, String uploaderId, String fileCategory);


}
