package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
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
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * 프로필 파일 업로드 또는 업데이트
     */
    @Transactional
    public FileMetadataDto uploadOrUpdateProfileFile(MultipartFile file, String uploaderId) {
        String category = "profile";

        // 기존 프로필 파일 조회
        FileMetadataDto existingMetadata = fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, category);

        if (existingMetadata != null) {
            // 기존 파일이 있을 경우 수정
            return fileService.modifyFile(file, existingMetadata.getId(), uploaderId, category);
        } else {
            // 기존 파일이 없을 경우 새 파일 업로드
            return fileService.uploadFile(file, uploaderId, category, null);
        }
    }

    /**
     * 프로필 파일 삭제
     */
    @Transactional
    public void deleteProfileFile(Long fileId, String uploaderId) {
        // 프로필 메타데이터 초기화 및 S3 파일 삭제
        fileService.deleteFile(fileId, uploaderId, "profile", true,  false);

        // 기존 메타데이터 조회
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File metadata not found for ID: " + fileId));
        metadata.setFileSize(null);
        metadata.setContentType(null);
        metadata.setDisplayName(null);
        metadata.setFileName(null);
        metadata.setFilePath(null);

        // 메타데이터 저장
        fileMetadataRepository.save(metadata);
    }

    /**
     * 프로필 url 조회
     */
    public FileMetadataDto getProfileUrl(String uploaderId) {

        return fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, "profile");
    }

    /**
     * 멤버 기본 프로필 메타데이터 생성
     */
    @Transactional
    public String createDefaultFileMetadataForMember(String uploaderId) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileCategory("profile");
        fileMetadata.setFileCategoryItemId(null);
        fileMetadata.setUploaderId(uploaderId);
        FileMetadata savedFile = fileMetadataRepository.save(fileMetadata);

        FileMetadataDto dto = FileMetadataDto.fromEntity(savedFile);

        return dto.getId().toString();
    }

}