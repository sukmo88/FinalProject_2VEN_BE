package com.sysmatic2.finalbe.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public class FileValidator {

    // 허용된 파일 확장자
    private static final Map<String, Set<String>> ALLOWED_EXTENSIONS_BY_CATEGORY = Map.of(
            "profile", Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg"),
            "icon", Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg"),
            "account", Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg"),
            "proposal", Set.of(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".zip", ".7z", ".rar", ".tar")
    );

    // 허용된 MIME 타입
    private static final Map<String, List<String>> ALLOWED_MIME_TYPES_BY_CATEGORY = Map.of(
            "profile", List.of("image/png", "image/jpeg", "image/gif", "image/webp"),
            "icon", List.of("image/png", "image/jpeg", "image/gif", "image/webp"),
            "account", List.of("image/png", "image/jpeg", "image/gif", "image/webp"),
            "proposal", List.of(
                    "application/pdf",
                    "application/msword", // .doc
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                    "application/vnd.ms-excel", // .xls
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                    "application/vnd.ms-powerpoint", // .ppt
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
                    "text/plain", // .txt
                    "application/zip"
            )
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한

    /**
     * 파일 전체 검증 메서드
     *
     * @param file        업로드된 파일
     * @param fileCategory 파일 카테고리 (예: "profile", "proposal")
     */
    public static void validateFile(MultipartFile file, String fileCategory) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or empty.");
        }

        validateFileCategory(fileCategory); // 파일 카테고리 검증
        validateFileExtension(fileName, fileCategory); // 파일 확장자 검증
        validateMimeType(file.getContentType(), fileCategory); // MIME 타입 검증
        //validateFileSize(file.getSize()); // 파일 크기 검증
    }

    /**
     * 파일 카테고리 검증
     */
    private static void validateFileCategory(String fileCategory) {
        if (fileCategory == null || fileCategory.isBlank()) {
            throw new IllegalArgumentException("File category cannot be null or blank.");
        }
        if (!ALLOWED_EXTENSIONS_BY_CATEGORY.containsKey(fileCategory)) {
            throw new IllegalArgumentException("Invalid file category: " + fileCategory);
        }
    }

    /**
     * 파일 확장자 검증
     */
    private static void validateFileExtension(String fileName, String fileCategory) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        Set<String> allowedExtensions = ALLOWED_EXTENSIONS_BY_CATEGORY.get(fileCategory);

        if (allowedExtensions == null || !allowedExtensions.contains(fileExtension)) {
            throw new IllegalArgumentException("Invalid file extension for category: " + fileCategory);
        }
    }

    /**
     * MIME 타입 검증
     */
    private static void validateMimeType(String mimeType, String fileCategory) {
        List<String> allowedMimeTypes = ALLOWED_MIME_TYPES_BY_CATEGORY.get(fileCategory);

        if (allowedMimeTypes == null || !allowedMimeTypes.contains(mimeType)) {
            throw new IllegalArgumentException("Invalid file type for category: " + fileCategory);
        }
    }

    /**
     * 파일 크기 검증
     */
    private static void validateFileSize(long fileSize) {
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds limit of 10MB.");
        }
    }
}