package com.sysmatic2.finalbe.attachment.controller;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.service.IconService;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files/icon")
@RequiredArgsConstructor
public class IconController {

    private final IconService iconService;

    /**
     * 아이콘 파일 업로드
     *
     * @param file        업로드할 파일
     * @return 업로드된 파일 메타데이터와 성공 메시지
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadIcon(@RequestParam("file") MultipartFile file) {

        FileMetadataDto fileMetadataDto = iconService.uploadIcon(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "fileUrl", fileMetadataDto.getFilePath(),
                "displayName", fileMetadataDto.getDisplayName(),
                "message", "File successfully uploaded"
        ));

    }

    /**
     * 아이콘 파일 수정
     *
     * @param file        업로드할 파일
     * @return 업로드된 파일 메타데이터와 성공 메시지
     */
    @PostMapping("/modify")
    public ResponseEntity<?> modifyIcon(@RequestParam("fileUrl") String fileUrl,
                                        @RequestParam("file") MultipartFile file) {

        FileMetadataDto fileMetadataDto = iconService.modifyIcon(file, fileUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "fileUrl", fileMetadataDto.getFilePath(),
                "displayName", fileMetadataDto.getDisplayName(),
                "message", "File successfully modified"
        ));

    }

    /**
     * 아이콘 파일 삭제
     * @param fileUrl  삭제할 파일의 url
     * @return 성공 메시지
     */
    @DeleteMapping
    public ResponseEntity<?> deleteIcon(@RequestParam("fileUrl") String fileUrl) {

        iconService.deleteIcon(fileUrl);

        return ResponseEntity.ok(Map.of(
                "message", "File successfully deleted"
        ));
    }

}
