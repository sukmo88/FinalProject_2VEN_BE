package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.attachment.dto.FileMetadataDto;
import com.sysmatic2.finalbe.attachment.entity.FileMetadata;
import com.sysmatic2.finalbe.attachment.repository.FileMetadataRepository;
import com.sysmatic2.finalbe.attachment.service.FileService;
import com.sysmatic2.finalbe.exception.*;
import com.sysmatic2.finalbe.strategy.dto.LiveAccountDataPageResponseDto;
import com.sysmatic2.finalbe.strategy.dto.LiveAccountDataResponseDto;
import com.sysmatic2.finalbe.strategy.entity.LiveAccountDataEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.LiveAccountDataRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveAccountDataService {

    private final StrategyRepository strategyRepository;
    private final FileService fileService;
    private final LiveAccountDataRepository liveAccountDataRepository;
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * 1. 실계좌 인증하는 이미지 등록
     *
     * @param file          등록할 실계좌 이미지 파일
     * @param uploaderId    업로더 ID
     * @param strategyId    전략 ID (실계좌 인증이 속하는 전략 ID)
     *
     * @return 해당 전략에 속한 실계좌 이미지 정보를 포함한 LiveAccountDataResponseDto
     */
    @Transactional
    public LiveAccountDataResponseDto uploadLiveAccountData(MultipartFile file, String uploaderId, Long strategyId) {
        String category = "liveaccount";

        // 1. Validation
        // displayName (2024.01.01 인지 확인)
        //validateDateFormat(displayName);

        // strategy가 유효한 전략인지 확인하고 조회
        validateStrategy(strategyId);
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy not found with ID: " + strategyId + "in live account data uploading."));

        // 2. s3에 이미지 파일 업로드 및 FileMetadata 데이터
        FileMetadataDto fileMetadataDto = fileService.uploadFile(file, uploaderId, category, strategyId.toString());

        // 3. LiveAccountData 데이터 등록
        LiveAccountDataEntity liveAccountDataEntity = new LiveAccountDataEntity();
        liveAccountDataEntity.setStrategy(strategyEntity);
        liveAccountDataEntity.setFileName(fileMetadataDto.getDisplayName());
        liveAccountDataEntity.setFileLink(fileMetadataDto.getFilePath());
        liveAccountDataEntity.setFileSize(fileMetadataDto.getFileSize());
        liveAccountDataEntity.setFileType(fileMetadataDto.getContentType());

        // LiveAccountData 저장
        try {
            LiveAccountDataEntity savedEntity = liveAccountDataRepository.save(liveAccountDataEntity);

            // 4. 응답
            return LiveAccountDataResponseDto.fromEntity(savedEntity);
        } catch (Exception e) {
            // LiveAccountData 저장 실패 시 커스텀 예외 발생
            throw new LiveAccountDataException("Failed to save Live Account Data in database.", e);
        }
    }


    /**
     * 2. 실계좌 이미지 목록
     *
     * @param page          pagenation에 page 값(default = 0)
     * @param pageSize      pagenation의 pageSize값(default = 8)
     * @param strategyId    전략 ID
     *
     * @return 해당 전략에 속한 실계좌 이미지 정보와 페이지 정보를 포함한 LiveAccountDataPageResponseDto
     */
    public LiveAccountDataPageResponseDto getLiveAccountDataList(int page, int pageSize, Long strategyId){

        // 1. 페이지네이션 요청 값 검증 및 페이지 객체 생성
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }

        // 페이지 요청 객체 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        // 2. strategy가 유효한 전략인지 확인하고 조회
        validateStrategy(strategyId);
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy not found with ID: " + strategyId + "in live account data uploading."));

        // 3. 전략에 등록된 실계좌 인증 리스트 조회
        Page<LiveAccountDataEntity> liveAccountDataList = liveAccountDataRepository.findAllByStrategy(strategyEntity, pageable);

        // DTO 변환
        List<LiveAccountDataResponseDto> liveAccountDataDtos = liveAccountDataList.getContent()
                .stream()
                .map(LiveAccountDataResponseDto::fromEntity)
                .toList();

        // 4. 응답 반환
        return new LiveAccountDataPageResponseDto(
                liveAccountDataDtos,
                liveAccountDataList.getSize(),
                liveAccountDataList.getTotalPages(),
                liveAccountDataList.isLast(),
                liveAccountDataList.getTotalElements(),
                liveAccountDataList.getSort().isSorted(),
                liveAccountDataList.isFirst(),
                liveAccountDataList.getNumber(),
                Instant.now().toString()
        );
    }

    /**
     * 3-1. 실계좌 이미지 삭제 - 단일 삭제
     *
     * @param strategyId 전략 Id
     * @param liveAccountId 실계좌 인증 Id
     */
    @Transactional
    public void deleteLiveAccountData(Long strategyId, Long liveAccountId) {
        String category = "liveaccount";

        // 1. validation
        // 전략 유효성 확인
        validateStrategy(strategyId);

        // 2. 실계좌 인증 및 FileMetadata 조회
        LiveAccountDataEntity liveAccountDataEntity = liveAccountDataRepository.findById(liveAccountId)
                .orElseThrow(() -> new LiveAccountDataNotFoundException("LiveAccountData not found Id : " + liveAccountId));

        FileMetadata fileMetadata = fileMetadataRepository.findByFilePath(liveAccountDataEntity.getFileLink())
                .orElseThrow(() -> new FileMetadataNotFoundException("FileMetadata not found liveAccount fileLink : " + liveAccountDataEntity.getFileLink()));

        try {
            // 3. 실계좌 인증 데이터 삭제
            liveAccountDataRepository.delete(liveAccountDataEntity);

            // 4. S3 파일 삭제 및 파일 메타데이터 삭제
            fileService.deleteFile(fileMetadata.getId(), fileMetadata.getUploaderId(), category, true, true);

        } catch (Exception e) {
            throw new LiveAccountDataException("Failed to delete Live Account Data in database.", e);
        }
    }

    /**
     * 3-2. 실계좌 이미지 삭제 - 모두 삭제 (전략 삭제시)
     *
     * @param strategyId 전략 Id
     */
    @Transactional
    public void deleteAllLiveAccountData(Long strategyId) {
        String category = "liveaccount";

        // 1. validation
        // 전략 유효성 확인 및 조회
        validateStrategy(strategyId);
        StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new StrategyNotFoundException("Strategy not found with ID: " + strategyId + "in live account data uploading."));


        // 2. 실계좌 인증 리스트 조회
        List<LiveAccountDataEntity> liveAccountDataEntities = liveAccountDataRepository.findAllByStrategy(strategyEntity);

        if (liveAccountDataEntities.isEmpty()) {
            throw new LiveAccountDataNotFoundException("No LiveAccountData found for strategy ID: " + strategyId);
        }

        try {
            // 3. 실계좌 인증 데이터 삭제
            List<String> fileLinks = liveAccountDataEntities.stream()
                    .map(LiveAccountDataEntity::getFileLink)
                    .toList();

            List<FileMetadata> fileMetadataList = fileMetadataRepository.findAllByFilePathIn(fileLinks);

            if (fileMetadataList.size() != fileLinks.size()) {
                throw new FileMetadataNotFoundException("Some FileMetadata entries not found for live account file links.");
            }

            // DB에서 실계좌 데이터 삭제
            liveAccountDataRepository.deleteAll(liveAccountDataEntities);

            // S3 및 파일 메타데이터 삭제
            for (FileMetadata fileMetadata : fileMetadataList) {
                fileService.deleteFile(fileMetadata.getId(), fileMetadata.getUploaderId(), category, true, true);
            }

        } catch (Exception e) {
            throw new LiveAccountDataException("Failed to delete all Live Account Data for strategy ID: " + strategyId, e);
        }
    }



    /**
     * Validation - displayName 형식 확인
     */
    public boolean isValidDate(String displayName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        try {
            LocalDate.parse(displayName, formatter);
            return true; // 유효한 날짜 형식
        } catch (DateTimeParseException e) {
            return false; // 유효하지 않은 날짜 형식
        }
    }

    /**
     * Validation - displayName 형식 확인 및 예외 발생
     */
    public void validateDateFormat(String displayName) {
        if (!isValidDate(displayName)) {
            throw new InvalidFileNameException("The provided file name '" + displayName + "' does not follow the required format.");
        }
    }

    /**
     * Validation - 전략 존재 유무 확인 및 예외 발생
     */
    public void validateStrategy(Long strategyId) {
        if (!strategyRepository.existsById(strategyId)) {
            throw new StrategyNotFoundException("Strategy not found with ID: " + strategyId + "in live account data.");
        }
    }
}
