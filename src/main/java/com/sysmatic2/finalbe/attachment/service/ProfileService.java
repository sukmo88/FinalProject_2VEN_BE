package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final FileService fileService;
    private final S3ClientService s3ClientService;

    /**
     * Profile 파일 업로드
     */
    @Transactional
    public FileMetadata uploadOrUpdateProfileFile(MultipartFile file, String uploaderId) {
        String category = "profile";

        // 이미지 검증
        FileValidator.validateFile(file, category);

        // 기존 프로필 파일 조회
        Optional<FileMetadata> existingMetadata = fileService.getProfileMetadataByUploaderId(uploaderId, category);

        if (existingMetadata.isPresent()) {
            // 기존 파일 삭제
            FileMetadata metadata = existingMetadata.get();
            String s3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), category, metadata.getFileName());
            s3ClientService.deleteFile(s3Key);

            // 새로운 파일 정보 업데이트
            return fileService.updateProfile(file,uploaderId,category, metadata);
        } else {
            // 새 프로필 등록
            // 프로필 업로드는 fileCategoryItemId가 필요 없으므로 null 전달
            return fileService.uploadFile(file, uploaderId, category, null);
        }

    }

    /**
     * Profile 파일 다운로드 (Base64로 변환)
     */
    public String downloadProfileFileAsBase64(Long fileId, String uploaderId) {
        // `FileService`의 이미지 전용 다운로드 메서드 호출
        return fileService.downloadImageFile(fileId, uploaderId, "profile");
    }

    /**
     * Profile 파일 삭제
     * - S3에서 데이터 삭제
     * - 프로필 필드 초기화
     */
    @Transactional
    public void deleteProfileFile(Long fileId, String uploaderId) {
        String category = "profile";

        String fileName;
        try {
            // 1. 프로필 메타데이터 초기화
            fileName = fileService.clearFileMetadata(fileId);
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("File name is missing. Unable to delete file from S3.");
            }

            // 2. S3 파일 삭제
            fileService.deleteS3File(uploaderId, category, fileName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete profile file: " + e.getMessage(), e);
        }
    }

    /**
     * Profile 메타데이터 조회
     */
    public FileMetadata getProfileFileMetadata(Long fileId, String uploaderId) {
        FileMetadata metadata = fileService.getFileMetadata(fileId);

        // Profile 파일 접근 권한 확인
        if (!metadata.getUploaderId().equals(uploaderId) || !"profile".equals(metadata.getFileCategory())) {
            throw new IllegalArgumentException("Access denied or invalid category.");
        }

        return metadata;
    }

    /**
     * Member fileId 필드값 생성
     */
    @Transactional
    public String createDefaultFileMetadataForMember(String uploaderId) {
        // FileMetadata 객체 생성 및 기본 값 설정
        FileMetadata metadata = new FileMetadata();
        metadata.setFileCategory("profile");
        metadata.setUploaderId(uploaderId);

        // FileMetadata 저장
        FileMetadata newFileMetadata = fileService.saveMetadata(metadata); // fileService를 통해 메타데이터 저장

        return newFileMetadata.getId().toString();
    }

}