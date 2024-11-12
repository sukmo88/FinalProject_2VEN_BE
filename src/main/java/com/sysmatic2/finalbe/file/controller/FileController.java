package com.sysmatic2.finalbe.file.controller;

import com.sysmatic2.finalbe.file.entity.File;
import com.sysmatic2.finalbe.file.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("memberId") Long memberId,
            @RequestParam("fileType") String fileType,
            @RequestParam("relatedEntity") String relatedEntity,
            @RequestParam("relatedEntityId") Long relatedEntityId) {
        try {
            File uploadedFile = fileService.uploadFile(file, memberId, fileType, relatedEntity, relatedEntityId);
            return ResponseEntity.ok(uploadedFile);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }
}
