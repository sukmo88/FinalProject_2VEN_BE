package com.sysmatic2.finalbe.strategy.dto;

import com.sysmatic2.finalbe.strategy.entity.LiveAccountDataEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LiveAccountDataResponseDto {

    private Long liveAccountId; // 실게좌 인증 ID
    private String fileName; // 실계좌 제목
    private String fileLink; // 실계좌 링크
    private Long fileSize; // 실계좌 사이즈
    private String fileType; // 실계좌 파일 확장자

    // 정적 팩토리 메서드
    public static LiveAccountDataResponseDto fromEntity(LiveAccountDataEntity entity) {
        LiveAccountDataResponseDto dto = new LiveAccountDataResponseDto();
        dto.liveAccountId = entity.getLiveAccountId();
        dto.fileName = entity.getFileName();
        dto.fileLink = entity.getFileLink();
        dto.fileSize = entity.getFileSize();
        dto.fileType = entity.getFileType();
        return dto;
    }
}