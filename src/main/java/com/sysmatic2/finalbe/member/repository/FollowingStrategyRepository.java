package com.sysmatic2.finalbe.member.repository;

import com.sysmatic2.finalbe.member.dto.FollowingStrategyListDto;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyEntity;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyFolderEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowingStrategyRepository extends JpaRepository<FollowingStrategyEntity, Long> {
    void deleteAllByStrategy(StrategyEntity strategy);
    FollowingStrategyEntity findByFollowingStrategyFolderAndStrategy(FollowingStrategyFolderEntity followingStrategyFolder, StrategyEntity strategy);
    FollowingStrategyEntity findByStrategyAndMember(StrategyEntity strategy, MemberEntity member);
    //FollowingStrategyEntity findByStrategyAndMemberAndFollowingStrategyFolder(StrategyEntity strategy, MemberEntity member, Long folderId);
    int deleteByFollowingStrategyId(Long followingStrategyId);

    int deleteByStrategy(StrategyEntity strategy);
    int deleteByStrategyAndMember(StrategyEntity strategy, MemberEntity member);
    boolean existsByFollowingStrategyId(Long followingStrategyId);

    int countByFollowingStrategyFolder(FollowingStrategyFolderEntity followingStrategyFolder);

    //해당 회원이 해당 전략을 팔로우 했는지 여부
    boolean existsByStrategyAndMember(StrategyEntity strategy,MemberEntity member);
    //해당 폴더에 해당 전략이 등록되어있는지 여부
    boolean existsByStrategyAndFollowingStrategyFolder(StrategyEntity strategy,FollowingStrategyFolderEntity followingStrategyFolder);


    //Page<FollowingStrategyListDto> findAllAsFollowingStrategyListDto(Pageable pageable);

    //Page<FollowingStrategyEntity> findByStats(String stats, Pageable pageable);

    //관심전략폴더ID에 등록된 전략ID의 리스트 정보를 조회

//        @Query("SELECT new com.sysmatic2.finalbe.member.dto.FollowingStrategyListDto("
//                +"f.followingStrategyId, s.followers_count, s.kp_ratio, s.smScore, s.StrategyTitle) " +
//                "FROM FollowingStrategyEntity f, StrategyEntity s"+
//                "WHERE f.StrategyEntity = s.StrategyEntity"+
//                "AND f.followingStrategyFolder =:follwingStrategyFolder")

    @Query("SELECT new com.sysmatic2.finalbe.member.dto.FollowingStrategyListDto(" +
            "f.followingStrategyId, s.strategyId, s.followersCount, s.kpRatio, s.smScore, s.strategyTitle) " +
            "FROM FollowingStrategyEntity f " +
            "JOIN f.strategy s " +
            "WHERE f.followingStrategyFolder = :followingStrategyFolder AND s.isPosted = 'y' " +
            "AND s.isApproved = 'y'")
    List<FollowingStrategyListDto> getListFollowingStrategy1(@Param("followingStrategyFolder") FollowingStrategyFolderEntity followingStrategyFolder);

    @Query("SELECT new com.sysmatic2.finalbe.member.dto.FollowingStrategyListDto(" +
            "f.followingStrategyId, s.strategyId, s.followersCount, s.kpRatio, s.smScore, s.strategyTitle) " +
            "FROM FollowingStrategyEntity f " +
            "JOIN f.strategy s " +
            "WHERE f.followingStrategyFolder = :followingStrategyFolder AND s.isPosted = 'y' " +
            "AND s.isApproved = 'y'")
    Page<FollowingStrategyListDto> getListFollowingStrategyPage(@Param("followingStrategyFolder") FollowingStrategyFolderEntity followingStrategyFolder, Pageable pageable);

    @Query("SELECT s.strategyId " +
            "FROM FollowingStrategyEntity f " +
            "JOIN f.strategy s " +
            "WHERE f.followingStrategyFolder = :followingStrategyFolder AND s.isPosted = 'y' " +
            "AND s.isApproved = 'y'")
    List<Long> getListFollowingStrategyList(@Param("followingStrategyFolder") FollowingStrategyFolderEntity followingStrategyFolder);




}
