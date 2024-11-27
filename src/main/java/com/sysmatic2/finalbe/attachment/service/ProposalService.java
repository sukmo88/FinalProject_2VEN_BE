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
public class ProposalService {

    private final FileService fileService;
    private final FileMetadataRepository fileMetadataRepository;


    /**
     * 새로운 제안서 파일 등록
     */
    @Transactional
    public FileMetadataDto uploadProposal(MultipartFile file, String uploaderId, String strategyId) {
        String category = "proposal";

        // 새로운 제안서 등록
        return fileService.uploadFile(file, uploaderId, category,  strategyId);

    }

    /**
     * 제안서 파일 수정
     */
    @Transactional
    public FileMetadataDto modifyProposal(MultipartFile file, String filePath, String uploaderId, String strategyId) {
        String category = "proposal";

        // 기존 아이콘 파일 조회
        FileMetadataDto existingMetadataDto = fileService.getFileMetadataByFilePath(filePath);

        if (!existingMetadataDto.getFileCategory().equals(category)) {
            throw new IllegalArgumentException("Invalid category: Expected 'proposal', got " + existingMetadataDto.getFileCategory());
        }

        return fileService.modifyFile(file, existingMetadataDto.getId(), uploaderId, category);
    }


    /**
     * 제안서 파일 삭제
     */
    @Transactional
    public String deleteProposal(String filePath, String uploaderId) {
        String category = "proposal";

        FileMetadataDto existingMetadataDto = fileService.getFileMetadataByFilePath(filePath);

        // 제안서 메타데이터 초기화 및 S3 파일 삭제
        fileService.deleteFile(existingMetadataDto.getId(), uploaderId, category, true,  true);

        return existingMetadataDto.getId().toString();
    }

}
