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
        return fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, category)
                .map(metadata -> fileService.modifyFile(file, metadata.getId(), uploaderId, category)) // 기존 파일 업데이트
                .orElseGet(() -> fileService.uploadFile(file, uploaderId, category, null)); // 새 파일 업로드
    }

    /**
     * 프로필 파일 다운로드 (Base64로 변환되서 리턴)
     */
    public Object downloadProfileFileAsBase64(Long fileId, String uploaderId) {
        String category = "profile";

        // Base64 데이터 다운로드
        return fileService.downloadFile(fileId, uploaderId, category);
    }

    /**
     * 프로필 파일 삭제
     */
    @Transactional
    public void deleteProfileFile(Long fileId, String uploaderId) {
        // 프로필 메타데이터 초기화 및 S3 파일 삭제
        fileService.deleteFile(fileId, uploaderId, "profile", true, true);
    }

    /**
     * 프로필 메타데이터 조회
     */
    public FileMetadataDto getProfileFileMetadata(Long fileId, String uploaderId) {
        return fileService.getFileMetadataByFileIdAndUploaderIdAndCategory(fileId, uploaderId, "profile");
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