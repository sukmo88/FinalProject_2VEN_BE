package com.sysmatic2.finalbe.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FileValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp",
            ".pdf", ".doc", ".docx",
            ".xls", ".xlsx",
            ".ppt", ".pptx",
            ".txt", ".zip", ".7z", ".rar", ".tar"
    );

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/png", "image/jpeg", "image/gif", "image/webp",
            "application/pdf",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-powerpoint", // .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
            "text/plain", // .txt
            "application/zip"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        validateFileExtension(fileName);
        validateMimeType(file.getContentType());
        validateFileSize(file.getSize());
    }

    private static void validateFileExtension(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Invalid file extension");
        }
    }

    private static void validateMimeType(String mimeType) {
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    private static void validateFileSize(long fileSize) {
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds limit (10MB)");
        }
    }

    public static void validateFileCategory(String fileCategory) {
        if (fileCategory == null || fileCategory.isBlank()) {
            throw new IllegalArgumentException("File category cannot be null or blank");
        }
    }
}
