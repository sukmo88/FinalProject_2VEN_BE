package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.DefaultFolderDeleteException;
import com.sysmatic2.finalbe.exception.DefaultFolderRenameException;
import com.sysmatic2.finalbe.exception.FolderNotFoundException;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.dto.FolderNameDto;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyEntity;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyFolderEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.FollowingStrategyFolderRepository;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowingStrategyFolderService {
    private final FollowingStrategyFolderRepository folderRepository;

    //기본폴더 생성
    public void createDefaultFolder(MemberEntity member) {
        FollowingStrategyFolderEntity folderEntity = new FollowingStrategyFolderEntity();
        folderEntity.setFolderName("기본 폴더");
        folderEntity.setIsActive("Y");
        folderEntity.setIsDefaultFolder("Y");
        folderEntity.setMember(member);
        folderEntity.setCreatedBy(member.getMemberId());
        folderEntity.setModifiedBy(member.getMemberId());
        folderEntity.setFolderCreationDate(LocalDateTime.now());
        folderRepository.save(folderEntity);
    }

    //관심전략폴더 생성
    public FollowingStrategyFolderDto createFolder(FolderNameDto folderNameDto, CustomUserDetails customUserDetails) {
        //관심전략 폴더 (기본폴더=> 폴더명: 기본 폴더, 기본폴더여부)
        FollowingStrategyFolderEntity folderEntity = new FollowingStrategyFolderEntity();
        MemberEntity member = customUserDetails.getMemberEntity();

        //관심전략폴더ID 발급하고 멤버ID 가져와서 폴더명
        folderEntity.setFolderName(folderNameDto.getFolderName());
        folderEntity.setIsActive("Y");
        folderEntity.setIsDefaultFolder("N");
        folderEntity.setFolderCreationDate(LocalDateTime.now());
        folderEntity.setMember(member);
        folderRepository.save(folderEntity);


        return new FollowingStrategyFolderDto(
                folderEntity.getFolderId(),
                folderEntity.getFolderName(),
                folderEntity.getModifiedAt(),
                folderEntity.getIsDefaultFolder()
        );
    }


    //관심전략폴더 삭제
    public void deleteFolder(Long folderId) {
      Optional <FollowingStrategyFolderEntity> folderOptional = folderRepository.findById((folderId));

        FollowingStrategyFolderEntity folderEntity = folderOptional.get();

        if("Y".equals(folderEntity.getIsDefaultFolder())){
            throw new DefaultFolderDeleteException("기본 폴더는 삭제할 수 없습니다.");
        }

        folderRepository.delete(folderEntity);
        //고려할점 해당 관심전략 폴더에 관심전략이 있을경우 같이 삭제되어야함

    }

    //회원ID가 등록한 폴더목록 조회
    public List<FollowingStrategyFolderDto> getFolderList(MemberEntity member){
        //List<FollowingStrategyFolderEntity> folderList = folderRepository.findByMember(member);
        List<FollowingStrategyFolderDto> folderList = folderRepository.findFolderDtosByMember(member);
        //dto
        //폴더명등 필요한 정보만 넘기자
        return folderList;
    }


    //관심전략폴더명 수정
    public FollowingStrategyFolderDto updateFolderName(Long folderId, FollowingStrategyFolderDto folderDto, CustomUserDetails customUserDetails) {

        MemberEntity member = customUserDetails.getMemberEntity();
        FollowingStrategyFolderEntity folderEntity = folderRepository.findByFolderIdAndMember(folderId, member)
                .orElseThrow(() -> new FolderNotFoundException("해당 폴더를 찾을 수 없습니다."));

        if("Y".equals(folderEntity.getIsDefaultFolder())){
            throw new DefaultFolderRenameException("기본 폴더명은 변경할 수 없습니다.");
        }

        folderEntity.setFolderName(folderDto.getFolderName());
        folderRepository.save(folderEntity);
        folderDto.setFolderName(folderEntity.getFolderName());
        folderDto.setIsDefaultFolder("N");
        folderDto.setModifiedAt(LocalDateTime.now());
        return new FollowingStrategyFolderDto(
                folderEntity.getFolderId(),
                folderEntity.getFolderName(),
                folderEntity.getModifiedAt(),
                folderEntity.getIsDefaultFolder()
        );

    }

    // 회원탈퇴를 위한 회원의 전체 관심전략폴더 삭제
    @Transactional
    public void deleteFoldersByMember(MemberEntity member) {
        folderRepository.deleteAllByMember(member);
    }

}
