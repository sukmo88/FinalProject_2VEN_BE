package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.DuplicateFollowingStrategyException;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyListDto;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyRequestDto;
import com.sysmatic2.finalbe.member.dto.FollowingStrategyResponseDto;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyEntity;
import com.sysmatic2.finalbe.member.entity.FollowingStrategyFolderEntity;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.FollowingStrategyFolderRepository;
import com.sysmatic2.finalbe.member.repository.FollowingStrategyRepository;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowingStrategyService {
    private final FollowingStrategyRepository followingStrategyRepository;
    private final StrategyRepository strategyRepository;
    private final FollowingStrategyFolderRepository followingStrategyFolderRepository;

    //폴더별 관심전략 목록 조회 서비스
    public List<FollowingStrategyListDto> getListFollowingStrategy1(Long folderId){
//        FollowingStrategyListDto listDto = new FollowingStrategyListDto();
//        listDto.setStrategyName();//전략이름
//        listDto.setFollowers_count();
//        listDto.setSmScore();
//        listDto.setFollowingStrategyId();
//        listDto.setStrategyId();

       // ArrayList<FollowingStrategyListDto> list = new ArrayList<>();
        //폴더엔티티를 던져줘야해?
        FollowingStrategyFolderEntity folderEntity = followingStrategyFolderRepository.findById(folderId).get();
        List<FollowingStrategyListDto> list = followingStrategyRepository.getListFollowingStrategy1(folderEntity);

        return list;
    }

    //폴더별 관심전략 목록 조회 페이징
    public Page<FollowingStrategyListDto> getListFollowingStrategyPage(Pageable pageable,int size,Long folderId){
        //Pageable pageable = PageRequest.of(page, size);
        FollowingStrategyFolderEntity folderEntity = followingStrategyFolderRepository.findById(folderId).get();
        return followingStrategyRepository.getListFollowingStrategyPage(folderEntity,pageable);
    }

//    public Page<FollowingStrategyEntity> getEntitesByStatus(String status,int page,int size){
//        Pageable pageable = PageRequest.of(page, size);
//        return followingStrategyRepository.findByStats(status,pageable);
//    }

    //폴더ID 별 등록된 관심전략 폴더 Count 조회
    public int countFollowingStrategy(long folderId) {
        FollowingStrategyFolderEntity folderEntity = followingStrategyFolderRepository.findById(folderId).get();
        return followingStrategyRepository.countByFollowingStrategyFolder(folderEntity);
    }

    //관심 전략 등록 서비스
    @Transactional
    public FollowingStrategyResponseDto createFollowingStrategy(FollowingStrategyRequestDto requestDto, CustomUserDetails customUserDetails){
        FollowingStrategyResponseDto ResponseDto = new FollowingStrategyResponseDto();
        StrategyEntity strategyEntity = strategyRepository.findById(requestDto.getStrategyId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 전략이 존재하지 않습니다."));
        FollowingStrategyFolderEntity followingStrategyFolderEntity = followingStrategyFolderRepository.findById(requestDto.getFolderId())
                .orElseThrow(()-> new IllegalArgumentException("해당 ID의 폴더가 존재하지 않습니다."));
        //이미 등록된 관심전략 체크하는 로직 추가 필요
        //선택한 종목이 해당 관심 전략에 이미 등록되어 있습니다.
        //폴더에 해당 전략id가 있는지 체크
        FollowingStrategyEntity folllowingStrategeyEntity = followingStrategyRepository.findByFollowingStrategyFolderAndStrategy(followingStrategyFolderEntity,strategyEntity);

        if(folllowingStrategeyEntity != null){
               throw new DuplicateFollowingStrategyException("선택한 종목이 해당 관심 전략폴더에 이미 등록되어 있습니다.");
        }

       MemberEntity member = customUserDetails.getMemberEntity();
        if (member == null) {
            throw new IllegalStateException("회원 정보가 없습니다.");
        }

        LocalDateTime followedAt = LocalDateTime.now();

        FollowingStrategyEntity followingStrategyEntity = new FollowingStrategyEntity(
                followingStrategyFolderEntity,member,strategyEntity,followedAt);
        followingStrategyRepository.save(followingStrategyEntity);

        //관심전략 등록하면 전략의 follower_count 수 증가해줘야함
        strategyEntity.incrementFollowersCount();
        strategyRepository.save(strategyEntity);

        ResponseDto.setFolderId(requestDto.getFolderId());
        ResponseDto.setStrategyId(requestDto.getStrategyId());
        ResponseDto.setFollowingStrategyId(followingStrategyEntity.getFollowingStrategyId());
        ResponseDto.setStrategyName(strategyEntity.getStrategyTitle());

        return ResponseDto;
    }

    //관심 전략 삭제
    @Transactional
    public void deleteFollowingStrategy(FollowingStrategyRequestDto requestDto, CustomUserDetails customUserDetails){
        //삭제할 관심전략 조회
        Optional<FollowingStrategyEntity> followingStrategyEntityOptional = followingStrategyRepository.findById(requestDto.getFollowingStrategyId());

        if(followingStrategyEntityOptional.isEmpty()){
            throw new IllegalStateException("삭제할 관심전략이 존재하지 않습니다. 관심전략ID:" + requestDto.getFollowingStrategyId());
        }
        FollowingStrategyEntity followingStrategyEntity = followingStrategyEntityOptional.get();
        StrategyEntity strategyEntity = followingStrategyEntity.getStrategy();
        if(!followingStrategyEntity.getCreatedBy().equals(customUserDetails.getMemberId())){
            throw new IllegalStateException("해당 관심전략을 삭제 권한이 없습니다. 관심전략ID:" + requestDto.getFollowingStrategyId());
        }

        int resultCnt = followingStrategyRepository.deleteByFollowingStrategyId(requestDto.getFollowingStrategyId());

        if(resultCnt == 0){
            throw new IllegalStateException("삭제할 전략 ID가 존재하지 않습니다. 관심전략ID:" + requestDto.getFollowingStrategyId());
        }

        //관심전략 삭제하면 전략의 follower_count 수 감소시켜줘야함
        strategyEntity.decrementFollowersCount();
        strategyRepository.save(strategyEntity);
    }

}
