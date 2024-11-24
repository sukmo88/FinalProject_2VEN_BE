package com.sysmatic2.finalbe.attachment.controller;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.service.FileService;
import com.sysmatic2.finalbe.attachment.service.ProfileService;
import com.sysmatic2.finalbe.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/files/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    /**
     * 프로필 파일 업로드
     * - 클라이언트에서 파일 업로드 요청 시 처리.
     * - 업로드된 파일은 S3에 저장되고 메타데이터는 데이터베이스에 저장.
     * - uploaderId는 나중에 JWT 토큰에서 추출될 예정. (현재는 query 파라미터로 받음)
     */
    @PostMapping
    public ResponseEntity<?> uploadProfileFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam("uploaderId") String uploaderId) {

        try {
            // 프로필 업로드
            FileMetadataDto fileMetadataDto = new FileMetadataDto(profileService.uploadOrUpdateProfileFile(file, uploaderId));

            // 성공적으로 업로드된 파일 정보를 반환
            return ResponseEntity.status(201).body(Map.of(
                    "fileId", fileMetadataDto.getId(),
                    "fileUrl", fileMetadataDto.getFilePath(),
                    "displayName", fileMetadataDto.getDisplayName(),
                    "message", "File successfully uploaded"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseUtils.buildErrorResponse(
                    "BAD_REQUEST",
                    "IllegalArgumentException",
                    "Invalid input: Missing required fields",
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 프로필 파일 다운로드
     * - 요청된 파일 ID에 해당하는 프로필 이미지를 Base64 형식으로 반환.
     * - uploaderId는 파일 소유자 확인을 위해 필요.
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> downloadProfileFile(@PathVariable Long fileId, @RequestParam("uploaderId") String uploaderId) {

        try {
            // 파일 다운로드 처리
            String base64Content = profileService.downloadProfileFileAsBase64(fileId, uploaderId);
            FileMetadataDto fileMetadataDto = new FileMetadataDto(profileService.getProfileFileMetadata(fileId, uploaderId));

            // 성공적으로 다운로드된 파일 데이터를 반환
            return ResponseEntity.ok(Map.of(
                    "fileId", fileMetadataDto.getId(),
                    "fileUrl" , fileMetadataDto.getFilePath(),
                    "displayName", fileMetadataDto.getDisplayName(),
                    "message", "File successfully retrieved",
                    "base64Content", base64Content
            ));

        } catch (NoSuchElementException e) {
            return ResponseUtils.buildErrorResponse(
                    "FILE_NOT_FOUND",
                    "NoSuchElementException",
                    "file not found",
                    HttpStatus.BAD_REQUEST
            );
        } catch (IllegalArgumentException e) {
            return ResponseUtils.buildErrorResponse(
                    "BAD_REQUEST",
                    "IllegalArgumentException",
                    "Invalid input: Missing required fields",
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 프로필 파일 삭제
     * - 요청된 파일 ID에 해당하는 프로필 파일 삭제.
     * - S3와 데이터베이스에서 모두 삭제 처리.
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteProfileFile(@PathVariable Long fileId, @RequestParam("uploaderId") String uploaderId) {

        try {
            // 파일 삭제 처리
            profileService.deleteProfileFile(fileId, uploaderId);

            // 성공적으로 삭제된 파일 정보를 반환
            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "message", "File successfully deleted"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseUtils.buildErrorResponse(
                    "BAD_REQUEST",
                    "IllegalArgumentException",
                    "Invalid input: Missing required fields",
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR",
                    e.getClass().getSimpleName(),
                    "An unexpected error occurred on the server",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Profile 파일 메타데이터 조회
     */
    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<Map<String, Object>> getProfileFileMetadata(@PathVariable Long fileId,
                                                                      @RequestParam("uploaderId") String uploaderId) {

        FileMetadataDto fileMetadataDto = new FileMetadataDto(profileService.getProfileFileMetadata(fileId, uploaderId));

        return ResponseEntity.ok(Map.of(
                "fileId", fileMetadataDto.getId(),
                "fileUrl", fileMetadataDto.getFilePath(),
                "displayName", fileMetadataDto.getDisplayName(),
                "fileSize", fileMetadataDto.getFileSize(),
                "contentType", fileMetadataDto.getContentType(),
                "uploaderId", fileMetadataDto.getUploaderId(),
                "category", fileMetadataDto.getFileCategory(),
                "createdAt", fileMetadataDto.getUploadedAt(),
                "message", "File metadata retrieved successfully"
        ));
    }

}