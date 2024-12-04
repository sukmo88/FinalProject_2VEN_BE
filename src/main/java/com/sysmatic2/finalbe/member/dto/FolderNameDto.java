package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FolderNameDto {
    @NotNull(message = "폴더명은 필수 입력 값입니다.") String folderName;
}
