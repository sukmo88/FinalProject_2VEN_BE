package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IconService {

    private final FileService fileService;
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * 새로운 아이콘 이미지 등록
     */
    @Transactional
    public Map<String, ?> uploadIcon(MultipartFile file) {
        String category = "icon";
        String uploaderId = "admin";

        // 새로운 아이콘 등록
        FileMetadataDto dto = fileService.uploadFile(file, uploaderId, category, null);

        return Map.of(
                "fileUrl", dto.getFilePath(),
                "message", "File successfully uploaded"
        );
    }

    /**
     * 기존 아이콘 이미지 수정
     */
    @Transactional
    public Map<String, ?> modifyIcon(MultipartFile file, String filePath) {
        String category = "icon";
        String uploaderId = "admin";

        // 기존 아이콘 파일 조회
        FileMetadataDto existingMetadataDto = fileService.getFileMetadataByFilePath(filePath);

        if (!existingMetadataDto.getFileCategory().equals(category)) {
            throw new IllegalArgumentException("Invalid category: Expected 'icon', got " + existingMetadataDto.getFileCategory());
        }

        // 아이콘 정보 업데이트
        FileMetadataDto dto = fileService.modifyFile(file, existingMetadataDto.getId(), uploaderId, category);

        return Map.of(
                "fileUrl", dto.getFilePath(),
                "message", "File successfully modified"
        );
    }

    /**
     * 아이콘 파일 삭제
     */
    @Transactional
    public Map<String, ?> deleteIcon(String filePath) {
        String category = "icon";
        String uploaderId = "admin";

        FileMetadataDto existingMetadataDto = fileService.getFileMetadataByFilePath(filePath);


        // 프로필 메타데이터 초기화 및 S3 파일 삭제
        fileService.deleteFile(existingMetadataDto.getId(), uploaderId, category, true,  false);

        // 기존 메타데이터 조회
        FileMetadata metadata = fileMetadataRepository.findById(existingMetadataDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("File metadata not found for ID: " + existingMetadataDto.getId()));
        metadata.setFileSize(null);
        metadata.setContentType(null);
        metadata.setDisplayName(null);
        metadata.setFileName(null);
        metadata.setFilePath(null);

        // 메타데이터 저장
        fileMetadataRepository.save(metadata);

        return Map.of(
                "message", "File successfully deleted"
        );
    }


}
