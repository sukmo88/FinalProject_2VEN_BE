package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.dto.InvestmentAssetClassesDto;
import com.sysmatic2.finalbe.strategy.dto.InvestmentAssetClassesPayloadDto;
import com.sysmatic2.finalbe.strategy.entity.InvestmentAssetClassesEntity;
import com.sysmatic2.finalbe.strategy.repository.InvestmentAssetClassesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

//관리자 페이지 - 투자자산 분류 관리
@Service
@RequiredArgsConstructor
public class InvestmentAssetClassesService {
    private final InvestmentAssetClassesRepository iacRepository;

    //1. 투자자산 분류 전체 목록을 가져오는 메서드
    public List<InvestmentAssetClassesDto> getList() throws Exception{
        //TODO) 관리자 판별
        //엔티티의 값들을 DTO에 저장하고 DTO를 보낸다.
        List<InvestmentAssetClassesDto> dtoList = new ArrayList<>();

        for(InvestmentAssetClassesEntity iacEntity : iacRepository.findAll()){
            dtoList.add(new InvestmentAssetClassesDto(iacEntity.getInvestmentAssetClassesId(),
                    iacEntity.getOrder(), iacEntity.getInvestmentAssetClassesName(),
                    iacEntity.getInvestmentAssetClassesIcon(), iacEntity.getIsActive()));
        }
        return dtoList;
    }

    //1-1. 투자자산 분류 상세 조회 메서드
    public InvestmentAssetClassesDto getById(Integer id) throws Exception{
        //TODO) 관리자 판별
        //id 값으로 optional 객체 얻기
        Optional<InvestmentAssetClassesEntity> iacOptional = iacRepository.findById(id);
        //저장할 엔티티 객체 생성
        InvestmentAssetClassesEntity iacEntity;

        //만약 찾으면 엔티티 객체에 저장
        if(iacOptional.isPresent()){
            iacEntity = iacOptional.get();
        }else{
            //못찾으면 에러 메시지 보내기
            throw new NoSuchElementException();
        }

        //엔티티 객체를 DTO에 담아서 보낸다.
        InvestmentAssetClassesDto iasDto = new InvestmentAssetClassesDto(iacEntity.getInvestmentAssetClassesId(),
                iacEntity.getOrder(), iacEntity.getInvestmentAssetClassesName(), iacEntity.getInvestmentAssetClassesIcon(),
                iacEntity.getIsActive());

        return iasDto;
    }

    //2. 투자자산 분류 생성
    //페이로드 DTO를 받아서 엔티티에 넣는다. 만들고 나면 반환용 DTO를 반환한다.
    //TODO) 관리자 판별
    //TODO) 같은 순서 입력한 경우 에러발생 시키기
    public InvestmentAssetClassesDto register(InvestmentAssetClassesPayloadDto iacPayloadDto){
        //엔티티 객체 생성
        InvestmentAssetClassesEntity iacEntity = new InvestmentAssetClassesEntity();

        //받아온 payloadDTO값을 엔티티 객체에 넣는다.
        iacEntity.setOrder(iacPayloadDto.getOrder());
        iacEntity.setInvestmentAssetClassesName(iacPayloadDto.getInvestmentAssetClassesName());
        iacEntity.setInvestmentAssetClassesIcon(iacPayloadDto.getInvestmentAssetClassesIcon());
        iacEntity.setIsActive(iacPayloadDto.getIsActive());

        //최초 작성자, 최초 작성일시, 최종 수정자, 최종 수정일시 넣기
        //TODO) User 객체 받아오기 - spring security
        iacEntity.setCreatedBy(100L);
        iacEntity.setCreatedAt(LocalDateTime.now());
        iacEntity.setModifiedBy(100L);
        iacEntity.setModifiedAt(LocalDateTime.now());

        //save()
        iacRepository.save(iacEntity);

        //반환용 DTO에 엔티티의 값을 넣는다.
        InvestmentAssetClassesDto iacDto = new InvestmentAssetClassesDto();
        iacDto.setInvestmentAssetClassesId(iacEntity.getInvestmentAssetClassesId());
        iacDto.setOrder(iacEntity.getOrder());
        iacDto.setInvestmentAssetClassesName(iacEntity.getInvestmentAssetClassesName());
        iacDto.setInvestmentAssetClassesIcon(iacEntity.getInvestmentAssetClassesIcon());
        iacDto.setIsActive(iacEntity.getIsActive());

        return iacDto;
    }

    //3. 투자자산 분류 삭제
    //id 값 받으면 해당 id 데이터 삭제
    public void delete(Integer id) throws Exception{
        //TODO) 관리자 판별
        //id로 찾아보고
        Optional<InvestmentAssetClassesEntity> optionalIac = iacRepository.findById(id);
        InvestmentAssetClassesEntity iacEntity;

        if(optionalIac.isPresent()){
            //있으면 엔티티 객체에 저장
            iacEntity = optionalIac.get();
        } else {
            //없으면 에러
            throw new NoSuchElementException();
        }
        //deleteById()
        iacRepository.deleteById(id);
    }

    //4. 투자자산 분류 수정
    public  void update(Integer id, InvestmentAssetClassesPayloadDto iasPayloadDto) throws Exception{
        //TODO) 관리자 판별
        //수정한 내용을 덮어씌우는 느낌.
        Optional<InvestmentAssetClassesEntity> optionalIac = iacRepository.findById(id);
        InvestmentAssetClassesEntity iacEntity;

        if(optionalIac.isPresent()){
            //있으면 엔티티 객체에 저장
            iacEntity = optionalIac.get();
        }else{
            throw new NoSuchElementException();
        }

        //엔티티에 페이로드 내용 저장
        iacEntity.setOrder(iasPayloadDto.getOrder());
        iacEntity.setInvestmentAssetClassesName(iasPayloadDto.getInvestmentAssetClassesName());
        iacEntity.setInvestmentAssetClassesIcon(iasPayloadDto.getInvestmentAssetClassesIcon());
        iacEntity.setIsActive(iasPayloadDto.getIsActive());

        //save()
        iacEntity = iacRepository.save(iacEntity);
//        InvestmentAssetClassesDto iacDto = new InvestmentAssetClassesDto(iacEntity.getInvestmentAssetClassesId(),
//                iacEntity.getOrder(), iacEntity.getInvestmentAssetClassesName(),
//                iacEntity.getInvestmentAssetClassesIcon(), iacEntity.getIsActive());
//
//        return iacDto;
    }
}
