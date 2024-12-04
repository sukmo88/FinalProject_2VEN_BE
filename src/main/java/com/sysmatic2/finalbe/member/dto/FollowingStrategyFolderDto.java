package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
public class FollowingStrategyFolderDto {
    private Long folderId;

    @NotNull(message = "폴더명은 필수 입력 값입니다.")
    private String folderName;
    private LocalDateTime modifiedAt;
    private String isDefaultFolder;
    private Long strategyCount;

    public FollowingStrategyFolderDto(Long folderId, String folderName, LocalDateTime modifiedAt, String isDefaultFolder) {
        this.folderId = folderId;
        this.folderName = folderName;
        this.modifiedAt = modifiedAt;
        this.isDefaultFolder = isDefaultFolder;
    }

    public FollowingStrategyFolderDto(Long folderId, String folderName, LocalDateTime modifiedAt, String isDefaultFolder, Long strategyCount) {
        this.folderId = folderId;
        this.folderName = folderName;
        this.modifiedAt = modifiedAt;
        this.isDefaultFolder = isDefaultFolder;
        this.strategyCount = strategyCount;
    }
}
