package com.sysmatic2.finalbe.attachment.controller;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.service.ProfileService;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * 프로필 파일 업로드 또는 업데이트
     *
     * @param file        업로드할 파일
     * @param userDetails  업로드한 사용자 ID (JWT 토큰에서 추출)
     * @return 업로드된 파일 메타데이터와 성공 메시지
     */
    @PostMapping
    public ResponseEntity<?> uploadProfileFile(@RequestParam("file") MultipartFile file,
                                               //@RequestParam("uploaderId") String uploaderId) {
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        // uploaderId 추출 (로그인한 사람)
        String uploaderId = userDetails.getMemberId();

        try {
            FileMetadataDto fileMetadataDto = profileService.uploadOrUpdateProfileFile(file, uploaderId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "fileId", fileMetadataDto.getId(),
                    "fileUrl", fileMetadataDto.getFilePath(),
                    "displayName", fileMetadataDto.getDisplayName(),
                    "message", "File successfully uploaded"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseUtils.buildErrorResponse(
                    "BAD_REQUEST", e.getClass().getSimpleName(),
                    "Invalid input: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR", e.getClass().getSimpleName(),
                    "An unexpected error occurred.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 프로필 파일 다운로드
     *
     * @param fileId      다운로드할 파일의 ID
     * @param uploaderId  요청한 사용자 ID (JWT 토큰에서 추출 예정)
     * @return Base64로 인코딩된 파일 데이터
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> downloadProfileFile(@PathVariable Long fileId,
                                                 @RequestParam("uploaderId") String uploaderId) {
        // Metadata 객체
        FileMetadataDto dto = profileService.getProfileFileMetadata(fileId, uploaderId);

        try {
            String base64Content = profileService.downloadProfileFileAsBase64(fileId, uploaderId).toString();

            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "displayName", dto.getDisplayName(),
                    "base64Content", base64Content,
                    "message", "File successfully retrieved"
            ));
        } catch (NoSuchElementException e) {
            return ResponseUtils.buildErrorResponse(
                    "FILE_NOT_FOUND", e.getClass().getSimpleName(),
                    "File not found.",
                    HttpStatus.NOT_FOUND
            );
        } catch (IllegalArgumentException e) {
            return ResponseUtils.buildErrorResponse(
                    "BAD_REQUEST", e.getClass().getSimpleName(),
                    "Invalid input: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR", e.getClass().getSimpleName(),
                    "An unexpected error occurred.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 프로필 파일 삭제
     *
     * @param fileId      삭제할 파일의 ID
     * @param userDetails  요청한 사용자 ID (JWT 토큰에서 추출)
     * @return 성공 메시지
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteProfileFile(@PathVariable Long fileId,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        // uploaderId 추출 (로그인한 사람)
        String uploaderId = userDetails.getMemberId();

        try {
            profileService.deleteProfileFile(fileId, uploaderId);

            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "message", "File successfully deleted"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseUtils.buildErrorResponse(
                    "BAD_REQUEST", e.getClass().getSimpleName(),
                    "Invalid input: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR", e.getClass().getSimpleName(),
                    "An unexpected error occurred.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 프로필 메타데이터 조회
     *
     * @param fileId      파일의 ID
     * @param uploaderId  요청한 사용자 ID
     * @return 파일 메타데이터
     */
    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<?> getProfileFileMetadata(@PathVariable Long fileId,
                                                    @RequestParam("uploaderId") String uploaderId) {
        try {
            FileMetadataDto fileMetadataDto = profileService.getProfileFileMetadata(fileId, uploaderId);

            return ResponseEntity.ok(Map.of(
                    "fileId", fileMetadataDto.getId(),
                    "fileUrl", fileMetadataDto.getFilePath(),
                    "displayName", fileMetadataDto.getDisplayName(),
                    "fileSize", fileMetadataDto.getFileSize(),
                    "contentType", fileMetadataDto.getContentType(),
                    "uploaderId", fileMetadataDto.getUploaderId(),
                    "category", fileMetadataDto.getFileCategory(),
                    "uploadedAt", fileMetadataDto.getUploadedAt(),
                    "message", "File metadata retrieved successfully"
            ));
        } catch (NoSuchElementException e) {
            return ResponseUtils.buildErrorResponse(
                    "FILE_NOT_FOUND", e.getClass().getSimpleName(),
                    "File metadata not found.",
                    HttpStatus.NOT_FOUND
            );
        } catch (IllegalArgumentException e) {
            return ResponseUtils.buildErrorResponse(
                    "BAD_REQUEST", e.getClass().getSimpleName(),
                    "Invalid input: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.buildErrorResponse(
                    "INTERNAL_SERVER_ERROR", e.getClass().getSimpleName(),
                    "An unexpected error occurred.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}