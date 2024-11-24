package com.sysmatic2.finalbe.attachment.repository;

import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;


public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findAllByUploaderIdAndFileCategory(String uploaderId, String fileCategory);
    Optional<FileMetadata> findFirstByUploaderIdAndFileCategory(String uploaderId, String fileCategory);


}
