package com.sysmatic2.finalbe.attachment.controller;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.service.FileService;
import com.sysmatic2.finalbe.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("fileCategory") String fileCategory,
                                        @RequestParam("uploaderId") String uploaderId) {
        try {
            // 파일과 파일 카테고리 null 및 빈 값 확인
            FileValidator.validateFile(file);
            FileValidator.validateFileCategory(fileCategory);

            // 파일 업로드 처리
            String fileId = fileService.uploadFile(file, uploaderId, fileCategory);

            return ResponseEntity.ok(Map.of(
                    "message", "File successfully uploaded",
                    "fileId", fileId)
            );

        } catch (IllegalArgumentException e) {
            // 잘못된 입력 값에 대한 에러 응답
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            // 기타 서버 에러에 대한 응답
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "An unexpected error occurred"
            ));
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<String> downloadFile(@PathVariable Long fileId) {
        // DB에서 파일 메타데이터 조회
        FileMetadata metadata = fileService.getFileMetadata(fileId);

        // Presigned URL 생성
        String presignedUrl = fileService.generatePresignedUrl(metadata.getFileName());

        // Presigned URL 반환
        return ResponseEntity.ok(presignedUrl);
    }


    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFileMetadata(@PathVariable Long fileId) {
        FileMetadata metadata = fileService.getFileMetadata(fileId);

        FileMetadataDto fileMetadataDto = new FileMetadataDto(metadata);

        return ResponseEntity.ok(fileMetadataDto);
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getAllFiles() {
        List<FileMetadata> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable("fileId") Long fileId,
                                        @RequestParam("fileCategory") String fileCategory,
                                        @RequestParam("uploaderId") String uploaderId) {
        try {
            fileService.deleteFile(fileId, uploaderId, fileCategory);
            return ResponseEntity.ok(Map.of("message", "File successfully deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "An unexpected error occurred while deleting the file"
            ));
        }
    }



    @GetMapping("/list")
    public ResponseEntity<?> getFilesByCategoryAndUploader(@RequestParam("fileCategory") String fileCategory,
                                                           @RequestParam("uploaderId") String uploaderId) {
        try {
            // 서비스 계층 호출
            List<FileMetadata> files = fileService.getFilesByCategoryAndUploader(fileCategory, uploaderId);

            // DTO로 변환하여 반환
            List<FileMetadataDto> fileMetadataDtos = files.stream()
                    .map(FileMetadataDto::new)
                    .toList();

            return ResponseEntity.ok(fileMetadataDtos);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "An error occurred while fetching the file list"
            ));
        }
    }

}
