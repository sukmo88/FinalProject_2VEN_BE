package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProposalService {

    private final FileService fileService;

    /**
     * 새로운 제안서 파일 등록
     */
    @Transactional
    public FileMetadataDto uploadProposal(MultipartFile file, String uploaderId) {
        String category = "proposal";

        // 새로운 제안서 등록
        return fileService.uploadFile(file, uploaderId, category, null, category);
    }

    /**
     * 제안서 파일 삭제
     */
    @Transactional
    public String deleteProposal(String filePath, String uploaderId) {
        String category = "proposal";

        FileMetadataDto existingMetadataDto = fileService.getFileMetadataByFilePath(filePath);

        // 제안서 메타데이터 및 S3 파일 삭제
        fileService.deleteFile(existingMetadataDto.getId(), uploaderId, category, true,  true);

        return existingMetadataDto.getId().toString();
    }

    /**
     * 제안서 파일 조회
     */
    public Optional<FileMetadataDto> getProposalUrlByFilePath(String filePath){
        // 제안서 조회 및 dto로 반환
        return Optional.ofNullable(fileService.getFileMetadataByFilePath(filePath));

    }
}
