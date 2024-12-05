package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.dto.FolderNameDto;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyListDto;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.FollowingStrategyRepository;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.member.service.FollowingStrategyFolderService;
import com.sysmatic2.finalbe.member.service.FollowingStrategyService;
import com.sysmatic2.finalbe.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final FollowingStrategyRepository followingStrategyRepository;
    private final FollowingStrategyService followingStrategyService;

    //신규 관심 전략 폴더 생성.
    @PostMapping("/following-strategy-folders")
    public ResponseEntity<Map<String,Object>> createFolder(@RequestBody FolderNameDto folderName,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        FollowingStrategyFolderDto dd = new FollowingStrategyFolderDto();
        String memberId = customUserDetails.getMemberId(); // 예: Member ID 추출
        FollowingStrategyFolderDto createdFolderDto = folderService.createFolder(folderName,customUserDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "폴더가 정상적으로 생성되었습니다.",
                "data", createdFolderDto
        ));
    }

    //특정 관심 전략 폴더 삭제.
    @DeleteMapping("/following-strategy-folders/{folderId}")
    public ResponseEntity<Map<String,Object>> deleteFolder(@PathVariable Long folderId,@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        folderService.deleteFolder(folderId,customUserDetails);
        return  ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message", "폴더가 정상적으로 삭제되었습니다."
        ));
    }


    //멤버별 관심 전략 폴더 목록 조회 페이징처리
    @GetMapping("/following-strategy-folderlist")
    public ResponseEntity<Map<String,Object>> getFolderListPage(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size){

        MemberEntity member = customUserDetails.getMemberEntity();
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "modifiedAt"));
        Pageable pageable = PageRequest.of(page, size);
        Page<FollowingStrategyFolderDto> folderListPage = folderService.getFolderListPage(member, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message","관심전략 목록이 정상적으로 조회되었습니다.",
                "data", folderListPage.getContent(),       // 페이징된 데이터
                "currentPage", folderListPage.getNumber(), // 현재 페이지
                "totalItems", folderListPage.getTotalElements(), // 전체 데이터 수
                "totalPages", folderListPage.getTotalPages() // 전체 페이지 수
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

}
