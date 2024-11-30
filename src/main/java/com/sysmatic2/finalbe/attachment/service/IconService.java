package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import com.sysmatic2.finalbe.exception.MetadataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IconService {

    private final FileService fileService;

    /**
     * 새로운 아이콘 이미지 등록
     */
    @Transactional
    public FileMetadataDto uploadIcon(MultipartFile file) {
        String category = "icon";
        String uploaderId = "admin";

        // 새로운 아이콘 등록
        return fileService.uploadFile(file, uploaderId, category, null);
    }

    /**
     * 기존 아이콘 이미지 수정
     */
    @Transactional
    public FileMetadataDto modifyIcon(MultipartFile file, String filePath) {
        String category = "icon";
        String uploaderId = "admin";

        // 기존 아이콘 파일 조회
        FileMetadataDto existingMetadataDto = fileService.getFileMetadataByFilePath(filePath);

        if (!existingMetadataDto.getFileCategory().equals(category)) {
            throw new IllegalArgumentException("Invalid category: Expected 'icon', got " + existingMetadataDto.getFileCategory());
        }

        // 아이콘 정보 업데이트
        return fileService.modifyFile(file, existingMetadataDto.getId(), uploaderId, category);
    }

    /**
     * 아이콘 파일 S3, DB 삭제
     */
    @Transactional
    public void deleteIcon(String filePath) {
        String category = "icon";
        String uploaderId = "admin";

        FileMetadataDto existingMetadataDto = fileService.getFileMetadataByFilePath(filePath);

        // existingMetadataDto가 없으면 커스텀 예외 처리
        if (existingMetadataDto == null) {
            throw new MetadataNotFoundException("Metadata not found for filePath: " + filePath);
        }

        // 제안서 메타데이터 초기화 및 S3 파일 삭제
        fileService.deleteFile(existingMetadataDto.getId(), uploaderId, category, true, true);
    }

}
