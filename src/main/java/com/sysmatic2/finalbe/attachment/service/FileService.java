package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3ClientService s3ClientService;
    private final FileMetadataRepository fileMetadataRepository;

    /* AWS S3에서 Presigned URL을 생성 메서드 */
    public String generatePresignedUrl(String fileName, String uploaderId, String category) {
        // 파일 메타데이터 조회
        FileMetadata metadata = fileMetadataRepository.findByFileNameAndUploaderIdAndFileCategory(fileName, uploaderId, category)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("File not found: fileName=%s, uploaderId=%s, category=%s", fileName, uploaderId, category)
                ));

        // S3 키 생성 (업로드 시 사용된 방식과 동일하게 생성)
        String s3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), metadata.getFileCategory(), metadata.getFileName());

        // S3 Presigned URL 생성
        return s3ClientService.generatePresignedUrl(s3Key, metadata.getDisplayName());
    }


    // DB에서 id로 fileName 가져오기
    public String getFileNameById(Long id) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + id));
        return fileMetadata.getFileName();
    }

    public String uploadFile(MultipartFile file, String uploaderId, String fileCategory) {
        Map fileNameInfo = s3ClientService.uploadFile(file, uploaderId, fileCategory);

        FileMetadata metadata = new FileMetadata();
        metadata.setDisplayName(file.getOriginalFilename());
        metadata.setFileName(fileNameInfo.get("fileName").toString());
        metadata.setFilePath(fileNameInfo.get("fileUrl").toString());
        metadata.setFileSize(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setFileCategory(fileCategory);
        metadata.setUploaderId(uploaderId);
        FileMetadata result = fileMetadataRepository.save(metadata);

        return result.getId().toString();
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files, String uploaderId, String fileCategory) {
        return files.stream()
                .map(file -> uploadFile(file, uploaderId, fileCategory))
                .toList();
    }


    public Resource downloadFile(String fileName) {
        String fileUrl = fileMetadataRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName))
                .getFilePath();

        try {
            return new UrlResource(fileUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file: " + fileName, e);
        }
    }

//    public String getFileUrl(String fileName) {
//        return fileMetadataRepository.findByFileName(fileName)
//                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName))
//                .getFilePath();
//    }

    public FileMetadata getFileMetadata(Long id) {
        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Metadata not found for file ID: " + id));
    }

    public List<FileMetadata> getFilesByCategoryAndUploader(String fileCategory, String uploaderId) {
        return fileMetadataRepository.findByFileCategoryAndUploaderId(fileCategory, uploaderId);
    }

    public List<FileMetadata> getAllFiles() {
        return fileMetadataRepository.findAll();
    }

    public void deleteFile(Long id, String userId, String category) {
        // DB에서 메타데이터 조회
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Metadata not found for file ID: " + id));

        if(!fileMetadataRepository.existsByIdAndFileCategoryAndUploaderId(metadata.getId(), category, userId)){
            throw new AccessDeniedException("You do not have permission to delete this file.");
        }

        // S3 키 생성
        String s3Key = s3ClientService.generateS3Key(userId, category, metadata.getFileName());

        try {
            // S3에서 파일 삭제
            s3ClientService.deleteFile(s3Key);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file from S3: " + metadata.getFileName(), e);
        }

        // DB에서 메타데이터 삭제
        fileMetadataRepository.delete(metadata);
    }

}
