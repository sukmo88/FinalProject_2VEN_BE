package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowingStrategyFolderDto {
    private Long folderId;

    @NotNull(message = "폴더명은 필수 입력 값입니다.")
    private String folderName;
    private LocalDateTime modifiedAt;
    private String isDefaultFolder;
}
