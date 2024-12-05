package com.sysmatic2.finalbe.member.repository;


import com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyListDto;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyFolderEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowingStrategyFolderRepository extends JpaRepository<FollowingStrategyFolderEntity, Long> {

    Optional<FollowingStrategyFolderEntity> findByFolderIdAndMember(Long folderId, MemberEntity member);

    @Query("SELECT new com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto(f.folderId, f.folderName, f.modifiedAt, f.isDefaultFolder, COUNT(fs)) " +
            "FROM FollowingStrategyFolderEntity f " +
            "LEFT JOIN FollowingStrategyEntity fs ON fs.followingStrategyFolder.folderId = f.folderId " +
            "WHERE f.member = :member " +
            "GROUP BY f.folderId, f.folderName, f.modifiedAt, f.isDefaultFolder")
    List<FollowingStrategyFolderDto> findFolderDtosByMember(@Param("member") MemberEntity member);


    List<FollowingStrategyFolderEntity> findByMember(MemberEntity member);

    void deleteAllByMember(MemberEntity member);

    @Query("SELECT new com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto(f.folderId, f.folderName, f.modifiedAt, f.isDefaultFolder, COUNT(fs)) " +
            "FROM FollowingStrategyFolderEntity f " +
            "LEFT JOIN FollowingStrategyEntity fs ON fs.followingStrategyFolder.folderId = f.folderId " +
            "WHERE f.member = :member " +
            "GROUP BY f.folderId, f.folderName, f.modifiedAt, f.isDefaultFolder")
    Page<FollowingStrategyFolderDto> getFolderListPage(MemberEntity member, Pageable pageable);

}
