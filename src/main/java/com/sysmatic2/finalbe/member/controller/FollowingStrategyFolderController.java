package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.member.service.FollowingStrategyFolderService;
import com.sysmatic2.finalbe.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class FollowingStrategyFolderController {

    private final FollowingStrategyFolderService folderService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    //신규 관심 전략 폴더 생성.
    @PostMapping("/following-strategy-folders")
    public ResponseEntity<Map<String,Object>> createFolder(@RequestBody FollowingStrategyFolderDto folderDto,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FollowingStrategyFolderDto dd = new FollowingStrategyFolderDto();
        String memberId = customUserDetails.getMemberId(); // 예: Member ID 추출
        FollowingStrategyFolderDto createdFolderDto = folderService.createFolder(folderDto,customUserDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "폴더가 정상적으로 생성되었습니다.",
                "data", createdFolderDto
        ));
    }

    //특정 관심 전략 폴더 삭제.
    @DeleteMapping("/following-strategy-folders/{folderId}")
    public ResponseEntity<Map<String,Object>> deleteFolder(@PathVariable Long folderId,@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        folderService.deleteFolder(folderId);
        return  ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message", "폴더가 정상적으로 삭제되었습니다."
        ));
    }

    //멤버별 관심 전략 폴더 목록 조회
    @GetMapping("/following-strategy-folderlist")
    public ResponseEntity<Map<String,Object>> getFolderList(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        String memberId = customUserDetails.getMemberId(); // 예: Member ID 추출
         Optional<MemberEntity> memberEntityOptional = memberRepository.findById(memberId);

        //List<FollowingStrategyFolderEntity> list =  folderService.getFolderList(memberEntityOptional.get());
        List<FollowingStrategyFolderDto> list = folderService.getFolderList(memberEntityOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message","폴더 리스트 조회 성공했습니다.",
                "data", list
        ));
    }

    //특정 관심 전략 폴더 이름 수정.
    @PutMapping("/following-strategy-folders/{folderId}")
    public ResponseEntity<Map<String,Object>> updateFolderName(@PathVariable Long folderId, @RequestBody FollowingStrategyFolderDto folderDto,@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FollowingStrategyFolderDto updatedFolderDto = folderService.updateFolderName(folderId, folderDto, customUserDetails);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message","폴더명 변경 성공했습니다.",
                "data",updatedFolderDto
        ));
    }

    /*
    //특정 관심 전략 폴더의 상세 정보 조회.
    @GetMapping("/following-strategy-folders/{folderId}")
    public ResponseEntity<FollowingStrategyFolderDto> getFolder(@PathVariable Long folderId) {
        FollowingStrategyFolderDto folder = folderService.getFolder(folderId);
        return ResponseEntity.ok(folder);
    }*/

    /*
    //특정 관심 폴더에 포함된 전략 조회.
    @GetMapping("/following-strategy-folders/{folderId}/strategies")
    public ResponseEntity<List<FollowingStrategyFolderDto>> getStrategiesInFolder(@PathVariable Long folderId) {
        List<FollowingStrategyFolderDto> strategies = folderService.getStrategiesInFolder(folderId);
        return ResponseEntity.ok(strategies);
    }
    //특정 전략을 관심 전략 폴더에 추가.
    @PostMapping("/following-strategy-folders/{folderId}/strategies/{strategyId}")
    public ResponseEntity<Void> addStrategyToFolder(@PathVariable Long folderId, @PathVariable Long strategyId) {
        folderService.addStrategyToFolder(folderId, strategyId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //특정 관심 전략 폴더에서 전략 삭제.
    @DeleteMapping("/following-strategy-folders/{folderId}/strategies/{strategyId}")
    public ResponseEntity<Void> removeStrategyFromFolder(@PathVariable Long folderId, @PathVariable Long strategyId) {
        folderService.removeStrategyFromFolder(folderId, strategyId);
        return ResponseEntity.noContent().build();
    }
    */
}
