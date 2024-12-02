package com.sysmatic2.finalbe.attachment.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import com.sysmatic2.finalbe.exception.*;
import com.sysmatic2.finalbe.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileMetadataRepository fileMetadataRepository;
    private final S3ClientService s3ClientService;

    /**
     * 파일 업로드
     */
    @Transactional
    public FileMetadataDto uploadFile(MultipartFile file, String uploaderId, String category, String fileCategoryItemId) {
        // 1. 파일 검증
        FileValidator.validateFile(file, category);

        // 2. 고유 파일 이름 및 S3 키 생성
        String originalFileName = (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
                ? file.getOriginalFilename() : "Unknown-File";
        String uniqueFileName = s3ClientService.generateUniqueFileName(originalFileName);
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        // 3. S3 업로드 및 FileMetadata 데이터 생성
        // 파일 URL 초기화
        String fileUrl = null;

        try {
            // S3 업로드
            fileUrl = s3ClientService.uploadFile(file, s3Key);

            // 새로운 파일 메타데이터 생성
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(uniqueFileName);
            metadata.setFilePath(fileUrl);
            metadata.setFileSize(file.getSize());
            metadata.setDisplayName(file.getOriginalFilename());
            metadata.setContentType(file.getContentType());
            metadata.setFileCategory(category);
            metadata.setUploaderId(uploaderId);


            // 카테고리 아이템 Id가 있는 경우,
            if (fileCategoryItemId != null) {
                metadata.setFileCategoryItemId(fileCategoryItemId);
            }

            // FileMetadata 저장
            FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

            // DTO로 변환 후 반환
            return FileMetadataDto.fromEntity(savedMetadata);

        } catch (Exception e) {
            // S3 업로드 성공 후 DB 저장 실패 시, S3에서 파일 삭제
            if (fileUrl != null) {
                s3ClientService.deleteFile(s3Key);
            }
            throw new S3FileUploadFiledException("Failed to upload file to S3: " + e.getMessage());
        }
    }


    /**
     * 파일 수정 (S3 업로드와 메타데이터 수정)
     */
    @Transactional
    public FileMetadataDto modifyFile(MultipartFile file, Long fileId, String uploaderId, String category) {
        // 파일 검증
        FileValidator.validateFile(file, category);

        // 기존 메타데이터 조회
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileMetadataNotFoundException("File metadata not found for ID: " + fileId));

        // 권한 및 카테고리 검증
        if (!metadata.getUploaderId().equals(uploaderId)) {
            throw new UnauthorizedAccessException("User does not have access to uploader ID: " + uploaderId);
        }
        if (!metadata.getFileCategory().equals(category)) {
            throw new InvalidCategoryException("Invalid category provided: " + category);
        }

        // 고유 파일 이름 및 S3 키 생성
        String originalFileName = file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()
                ? file.getOriginalFilename() : "unknownfile";
        String uniqueFileName = s3ClientService.generateUniqueFileName(originalFileName);
        String s3Key = s3ClientService.generateS3Key(uploaderId, category, uniqueFileName);

        String fileUrl;

        try {
            // 파일 업로드 (S3)
            fileUrl = s3ClientService.uploadFile(file, s3Key);

            // 기존 S3 파일 삭제
            String oldS3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), metadata.getFileCategory(), metadata.getFileName());
            s3ClientService.deleteFile(oldS3Key);

            // 메타데이터 업데이트
            metadata.setDisplayName(originalFileName);
            metadata.setFileName(uniqueFileName);
            metadata.setFilePath(fileUrl);
            metadata.setFileSize(file.getSize());
            metadata.setContentType(file.getContentType());

            // 메타데이터 저장
            FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

            // DTO로 변환하여 반환
            return FileMetadataDto.fromEntity(savedMetadata);

        } catch (Exception e) {
            // S3 업로드 성공 후 메타데이터 저장 실패 시 S3에서 삭제
            s3ClientService.deleteFile(s3Key);
            throw new S3FileUploadFiledException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    /**
     * 파일 삭제
     * - S3 파일 삭제
     * - 데이터베이스에서 삭제
     */
    @Transactional
    public void deleteFile(Long fileId, String uploaderId, String category, boolean deleteFromS3, boolean deleteFromDb) {
        // 1. 파일 접근 권한 검증 및 메타데이터 조회
        FileMetadata metadata = validateFileAccess(fileId, uploaderId, category);

        // 2. S3 파일 삭제 (필요한 경우)
        if (deleteFromS3) {
            String s3Key = s3ClientService.generateS3Key(metadata.getUploaderId(), metadata.getFileCategory(), metadata.getFileName());
            try {
                s3ClientService.deleteFile(s3Key);
            } catch (Exception e) {
                throw new S3FileDeleteFiledException("Failed to delete file from S3: " + e.getMessage(), e);
            }
        }

        // 3. DB에서 메타데이터 삭제 (필요한 경우)
        if (deleteFromDb) {
            try {
                fileMetadataRepository.delete(metadata);
            } catch (Exception e) {
                throw new FileMetadataDeleteFailedException("Failed to delete file metadata from database: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 파일 메타데이터 조회
     *
     * @param uploaderId 파일 업로더 ID
     * @param category 파일 카테고리
     * @return 파일 메타데이터
     */
    public FileMetadataDto getFileMetadataByUploaderIdAndCategory(String uploaderId, String category) {
        return fileMetadataRepository.findByUploaderIdAndFileCategory(uploaderId, category)
                .map(FileMetadataDto::fromEntity)
                .orElse(null); // 값이 없으면 null 반환
    }

    /**
     * 파일 메타데이터 조회
     *
     * @param filePath 파일 저장 경로
     * @return 파일 메타데이터
     */
    public FileMetadataDto getFileMetadataByFilePath(String filePath) {
        return fileMetadataRepository.findByFilePath(filePath)
                .map(FileMetadataDto::fromEntity)
                .orElse(null); // 값이 없으면 null 반환

    }

    /**
     * 파일 메타데이터 조회
     *
     * @param itemId 파일이 속한 곳의 Id (ex. strategyId)
     * @return 파일 메타데이터
     */
    public FileMetadataDto getFileMetadataByFileItemId(String itemId) {
        return fileMetadataRepository.findByFileCategoryItemId(itemId)
                .map(FileMetadataDto::fromEntity)
                .orElse(null); // 값이 없으면 null 반환
    }


    /**
     * 파일 메타데이터 리스트 조회
     *
     * @param itemId 파일이 속한 곳의 Id (ex. strategyId)
     * @return 파일 메타데이터
     */
    public List<FileMetadataDto> getAllByFileItemId(String itemId) {
        return fileMetadataRepository.findAllByFileCategoryItemId(itemId)
                .stream()
                .map(FileMetadataDto::fromEntity)
                .collect(Collectors.toList());
    }


    /**
     * 파일 접근 권한 및 유효성 검사 후 메타데이터 반환
     *
     * @param fileId     파일 ID
     * @param uploaderId 업로더 ID
     * @param category   파일 카테고리
     * @return 파일 메타데이터
     * @throws IllegalArgumentException 파일이 존재하지 않거나 조건에 맞지 않는 경우
     */
    public FileMetadata validateFileAccess(Long fileId, String uploaderId, String category) {
        // Check if the file exists
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileMetadataNotFoundException("The file does not exist fileId : " + fileId));

        // Validate uploader ID
        if (!metadata.getUploaderId().equals(uploaderId)) {
            throw new IllegalArgumentException("The uploader ID does not match.");
        }

        // Validate category
        if (!metadata.getFileCategory().equals(category)) {
            throw new IllegalArgumentException("The file category does not match.");
        }

        return metadata; // Return metadata if validation passes
    }

}