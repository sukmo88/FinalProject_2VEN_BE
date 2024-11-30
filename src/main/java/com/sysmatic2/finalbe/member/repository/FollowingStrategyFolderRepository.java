package com.sysmatic2.finalbe.member.repository;


import com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyFolderEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowingStrategyFolderRepository extends JpaRepository<FollowingStrategyFolderEntity, Long> {

    Optional<FollowingStrategyFolderEntity> findByfolderIdAndMember(Long folderId, MemberEntity member);

    @Query("SELECT new com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto(f.folderId, f.folderName, f.modifiedAt, f.isDefaultFolder) " +
            "FROM FollowingStrategyFolderEntity f " +
            "WHERE f.member = :member")
    List<FollowingStrategyFolderDto> findFolderDtosByMember(@Param("member") MemberEntity member);

}
