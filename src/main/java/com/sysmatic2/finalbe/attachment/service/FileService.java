package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileMetadataRepository fileMetadataRepository;
    private final S3ClientService s3ClientService;

    /**
     * 파일 업로드 : 새로운 파일 업로드, fileId 새로 생성
     */
    @Transactional
    public FileMetadata uploadFile(MultipartFile file, String uploaderId, String category, String fileCategoryItemId) {
        // 고유 파일 이름 및 S3 키 생성
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown-file";
        String uniqueFileName = s3ClientService.generateUniqueFileName(originalFileName);
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        // 새로운 파일 업로드
        String fileUrl = s3ClientService.uploadFile(file, s3Key);

        // 새로운 파일 메타데이터 생성 및 설정
        FileMetadata metadata = new FileMetadata();
        metadata.setDisplayName(originalFileName);
        metadata.setFileName(uniqueFileName);
        metadata.setFilePath(fileUrl);
        metadata.setFileSize(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setFileCategory(category);
        metadata.setUploaderId(uploaderId);

        // fileCategoryItemId가 있는 경우에만 설정
        if (fileCategoryItemId != null && !fileCategoryItemId.isEmpty()) {
            metadata.setFileCategoryItemId(fileCategoryItemId);
        }

        try {
            // 메타데이터 저장
            return fileMetadataRepository.save(metadata);
        } catch (Exception e) {
            // 메타데이터 저장 실패 시 S3에서 업로드된 파일 삭제
            s3ClientService.deleteFile(s3Key);
            throw new RuntimeException("Failed to save metadata: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 파일 업데이트 (기존 파일이 있는 경우, 메타데이터만 업데이트 - profile, icon, proposal)
     */
    @Transactional
    public FileMetadata updateProfile(MultipartFile file, String uploaderId, String category, FileMetadata metadata) {
        // 고유 파일 이름 및 S3 키 생성
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown-file";
        String uniqueFileName = s3ClientService.generateUniqueFileName(originalFileName);
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        // 새로운 파일 업로드
        String fileUrl = s3ClientService.uploadFile(file, s3Key);

        // 기존 메타데이터 업데이트
        metadata.setDisplayName(originalFileName);
        metadata.setFileName(uniqueFileName);
        metadata.setFilePath(fileUrl);
        metadata.setFileSize(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setFileCategory(category);
        metadata.setUploaderId(uploaderId);

        return fileMetadataRepository.save(metadata); // 기존 메타데이터 업데이트
    }

    /**
     * 파일 메타데이터 조회
     */
    public FileMetadata getFileMetadata(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + fileId));
    }

    /**
     * 파일 삭제
     * - S3 파일 삭제
     * - 데이터베이스에서 삭제
     */
    @Transactional
    public void deleteFile(Long fileId, String uploaderId, String category) {
        FileMetadata metadata = validateFileAccess(fileId, uploaderId, category);
        String s3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), metadata.getFileCategory(), metadata.getFileName());

        try {
            // S3에서 파일 삭제
            s3ClientService.deleteFile(s3Key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }

        try {
            // 데이터베이스에서 삭제
            fileMetadataRepository.delete(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file metadata from database: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 삭제
     * - S3 파일만 삭제
     */
    @Transactional
    public void deleteS3File(String uploaderId, String category, String fileName) {

        String s3Key = s3ClientService.generateS3Key(uploaderId, category, fileName);

        try {
            // S3에서 파일 삭제
            s3ClientService.deleteFile(s3Key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 삭제
     * - 데이터베이스에서 삭제
     */
    @Transactional
    public void deleteDbFile(Long fileId, String uploaderId, String category) {
        FileMetadata metadata = validateFileAccess(fileId, uploaderId, category);

        try {
            // 데이터베이스에서 삭제
            fileMetadataRepository.delete(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file metadata from database: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 다운로드 (Base64 또는 바이너리 데이터로 반환)
     */
    public Object downloadFile(Long fileId, String uploaderId, String category) {
        FileMetadata metadata = validateFileAccess(fileId, uploaderId, category);
        String s3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), metadata.getFileCategory(), metadata.getFileName());

        if (metadata.getContentType().startsWith("image/")) {
            return s3ClientService.downloadImageFileAsBase64(s3Key); // Base64 인코딩된 이미지
        } else {
            return s3ClientService.downloadDocumentFile(s3Key); // 바이너리 데이터
        }
    }

    /**
     * 파일 다운로드 - 이미지 (Base64로 반환)
     */
    public String downloadImageFile(Long fileId, String uploaderId, String category) {
        FileMetadata metadata = validateFileAccess(fileId, uploaderId, category);
        String s3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), metadata.getFileCategory(), metadata.getFileName());

        if (!metadata.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("File is not an image.");
        }

        return s3ClientService.downloadImageFileAsBase64(s3Key);
    }

    /**
     * 파일 다운로드 - 문서 (바이너리 데이터로 반환)
     */
    public byte[] downloadDocumentFile(Long fileId, String uploaderId, String category) {
        FileMetadata metadata = validateFileAccess(fileId, uploaderId, category);
        String s3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), metadata.getFileCategory(), metadata.getFileName());

        if (metadata.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("File is not a document.");
        }

        return s3ClientService.downloadDocumentFile(s3Key);
    }

    /**
     * 파일 접근 권한 검증 및 메타데이터 반환
     */
    private FileMetadata validateFileAccess(Long fileId, String uploaderId, String category) {
        if (uploaderId == null || uploaderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Uploader ID must be provided.");
        }

        FileMetadata metadata = getFileMetadata(fileId);
        if (!metadata.getUploaderId().equals(uploaderId) || !metadata.getFileCategory().equals(category)) {
            throw new AccessDeniedException(String.format(
                    "Access denied: File ID %d, Category %s, Uploader %s", fileId, category, uploaderId));
        }
        return metadata;
    }

    /**
     * 파일의 모든 리스트 반환
     */
    public List<Long> getAllFileIdList(String uploaderId, String category) {
        // 아이디, 카테고리로 모든 파일 리스트 가져오기
        List<FileMetadata> fileList = fileMetadataRepository.findAllByUploaderIdAndFileCategory(uploaderId, category);

        // 파일 아이디 리스트 반환하기
        return fileList.stream()
                .map(FileMetadata::getId)
                .toList();
    }

    /**
     * 파일 조회 메서드
     */
    public Optional<FileMetadata> getProfileMetadataByUploaderId(String uploaderId, String category) {
        return fileMetadataRepository.findFirstByUploaderIdAndFileCategory(uploaderId, category);
    }

    /**
     * DB 테이블의 필드 null로 초기화
     */
    @Transactional
    public String clearFileMetadata(Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + fileId));

        String fileName = metadata.getFileName();

        // 메타데이터 필드 초기화
        metadata.clearMetadata();

        return fileName;

        // 업데이트는 JPA가 자동으로 반영 (Transactional로 관리)
    }

    /**
     * 파일 메타데이터 저장
     */
    public FileMetadata saveMetadata(FileMetadata metadata) {
        return fileMetadataRepository.save(metadata);
    }

}