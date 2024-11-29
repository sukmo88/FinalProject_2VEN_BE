package com.sysmatic2.finalbe.attachment.controller;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.service.ProposalService;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files/proposal")
@RequiredArgsConstructor
public class ProposalController {
    private final ProposalService proposalService;

    /**
     * 제안서 파일 업로드 또는 업데이트
     *
     * @param file        업로드할 파일
     * @param userDetails  업로드한 사용자 ID (JWT 토큰에서 추출)
     * @return 업로드된 파일 메타데이터와 성공 메시지
     */
    @PostMapping
    public ResponseEntity<?> uploadProposal(@RequestParam("file") MultipartFile file,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        // uploaderId 추출 (로그인한 사람)
        String uploaderId = userDetails.getMemberId();

        FileMetadataDto fileMetadataDto = proposalService.uploadProposal(file, uploaderId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "fileId", fileMetadataDto.getId(),
                "fileUrl", fileMetadataDto.getFilePath(),
                "displayName", fileMetadataDto.getDisplayName(),
                "message", "File successfully uploaded"
        ));

    }

    /**
     * 제안서 파일 삭제
     *
     * @param fileUrl      삭제할 파일의 url
     * @param userDetails  요청한 사용자 ID (JWT 토큰에서 추출)
     * @return 성공 메시지
     */
    @DeleteMapping
    public ResponseEntity<?> deleteProposal(@RequestParam("fileUrl") String fileUrl,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        // uploaderId 추출 (로그인한 사람)
        String uploaderId = userDetails.getMemberId();

        String deletedFileId = proposalService.deleteProposal(fileUrl, uploaderId);

        return ResponseEntity.ok(Map.of(
                "fileId", deletedFileId,
                "message", "File successfully deleted"
        ));

    }

}
