package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.member.service.MemberHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final FileService fileService;
    private final MemberHelper memberHelper;

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
            FileMetadataDto fileMetadataDto =  fileService.modifyFile(file, existingMetadata.getId(), uploaderId, category);

            // 회원 정보의 fileId 업데이트
            memberHelper.initMemberFileId(uploaderId, fileMetadataDto.getId().toString(), fileMetadataDto.getFilePath());

            return fileMetadataDto;
        } else {
            // 새로운 파일 업로드
            FileMetadataDto fileMetadataDto = fileService.uploadFile(file, uploaderId, category, null);

            // 회원 정보의 fileId 업데이트
            memberHelper.initMemberFileId(uploaderId, fileMetadataDto.getId().toString(), fileMetadataDto.getFilePath());

            return fileMetadataDto;
        }
    }

    /**
     * 프로필 파일 삭제
     */
    @Transactional
    public void deleteProfileFile(Long fileId, String uploaderId) {
        // 프로필 메타데이터 및 S3 파일 삭제
        fileService.deleteFile(fileId, uploaderId, "profile", true,  true);

        // 회원 정보의 fileId, filePath 초기화
        memberHelper.initMemberFileId(uploaderId, null, null);
    }

    /**
     * 프로필 url 조회
     */
    public Optional<FileMetadataDto> getProfileUrl(String uploaderId) {

        return Optional.ofNullable(fileService.getFileMetadataByUploaderIdAndCategory(uploaderId, "profile"));
    }

}