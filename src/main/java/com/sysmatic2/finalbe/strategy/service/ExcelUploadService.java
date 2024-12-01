package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.dto.DailyStatisticsReqDto;
import com.sysmatic2.finalbe.exception.ExcelValidationException;
import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.StrategyEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelUploadService {

  private final Validator validator;
  private final StrategyRepository strategyRepository;
  private final DailyStatisticsRepository dailyStatisticsRepository;
  private static final int MAX_ROWS = 2000;
  private static final int EXPECTED_COLUMNS = 3;

  /**
   * 엑셀 파일의 데이터를 추출 및 저장
   *
   * @param file       업로드된 엑셀 파일
   * @param strategyId 전략 ID
   * @return 추출된 DailyStatisticsReqDto 리스트
   */
  public List<DailyStatisticsReqDto> extractAndSaveData(MultipartFile file, Long strategyId) {
    // 1. 엑셀 데이터 추출
    List<DailyStatisticsReqDto> dataList = extractAndValidateData(file);

    // 2. StrategyEntity 조회
    StrategyEntity strategyEntity = strategyRepository.findById(strategyId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid strategyId: " + strategyId));

    // 3. DailyStatisticsEntity로 변환 및 저장
    List<DailyStatisticsEntity> entities = new ArrayList<>();
    for (DailyStatisticsReqDto dto : dataList) {
      DailyStatisticsEntity entity = new DailyStatisticsEntity();
      entity.setStrategyEntity(strategyEntity);
      entity.setDate(dto.getDate());
      entity.setDepWdPrice(dto.getDepWdPrice());
      entity.setDailyProfitLoss(dto.getDailyProfitLoss());
      // 필요한 다른 필드들도 설정
      entities.add(entity);
    }

    dailyStatisticsRepository.saveAll(entities);

    return dataList; // 저장된 데이터를 반환
  }

  /**
   * 엑셀 파일의 데이터를 추출 및 유효성 검증
   *
   * @param file 업로드된 엑셀 파일
   * @return 추출된 DailyStatisticsReqDto 리스트
   */
  public List<DailyStatisticsReqDto> extractAndValidateData(MultipartFile file) {
    List<DailyStatisticsReqDto> excelData = new ArrayList<>();
    // 날짜와 해당 날짜가 처음 발견된 행 번호를 저장
    Map<LocalDate, Integer> dateMap = new HashMap<>();

    try (InputStream inputStream = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(inputStream)) {

      // 시트 수 검증: 첫 번째 시트만 허용
      if (workbook.getNumberOfSheets() > 1) {
        throw new ExcelValidationException("엑셀 파일에 여러 시트가 포함되어 있습니다. 첫 번째 시트만 허용됩니다.");
      }

      Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트만 처리

      int rowNumber = 0;
      for (Row row : sheet) {
        rowNumber++;

        // 최대 행 수 제한
        if (rowNumber > MAX_ROWS + 1) { // +1을 하는 이유는 첫 번째 행이 헤더일 수 있으므로
          throw new ExcelValidationException("엑셀 파일의 행 수가 2000개를 초과했습니다.");
        }

        // 첫 번째 행이 헤더인 경우 건너뜀
        if (rowNumber == 1) {
          continue;
        }

        // 칼럼 수 검증
        if (row.getPhysicalNumberOfCells() != EXPECTED_COLUMNS) {
          throw new ExcelValidationException("행 " + rowNumber + "의 칼럼 수가 정확히 " + EXPECTED_COLUMNS + "개가 아닙니다.");
        }

        DailyStatisticsReqDto dto = parseRowToDto(row, rowNumber);

        // 중복된 날짜 확인
        if (dateMap.containsKey(dto.getDate())) {
          int firstRowNumber = dateMap.get(dto.getDate());
          throw new ExcelValidationException("중복된 날짜가 발견되었습니다: " + dto.getDate() + " (행 " + firstRowNumber + ", " + rowNumber + ")");
        }
        dateMap.put(dto.getDate(), rowNumber);

        // Bean Validation을 사용한 추가 검증
        validateDto(dto, rowNumber);

        excelData.add(dto);
      }

      // 행 수가 0개인 경우 검증
      if (excelData.isEmpty()) {
        throw new ExcelValidationException("엑셀 파일에 데이터가 존재하지 않습니다.");
      }

    } catch (ExcelValidationException e) {
      throw e; // 커스텀 예외는 그대로 던짐
    } catch (Exception e) {
      throw new ExcelValidationException("엑셀 데이터 추출 중 오류가 발생했습니다: " + e.getMessage(), e);
    }

    return excelData;
  }

  /**
   * 엑셀 행을 DTO로 변환
   *
   * @param row       엑셀 행
   * @param rowNumber 행 번호 (오류 메시지용)
   * @return DailyStatisticsReqDto 객체
   */
  private DailyStatisticsReqDto parseRowToDto(Row row, int rowNumber) {
    try {
      Cell dateCell = row.getCell(0);
      Cell depWdPriceCell = row.getCell(1);
      Cell dailyProfitLossCell = row.getCell(2);

      LocalDate date = null;
      if (dateCell != null) {
        if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
          date = dateCell.getLocalDateTimeCellValue().toLocalDate();
        } else if (dateCell.getCellType() == CellType.STRING) {
          // 문자열로 된 날짜 처리
          try {
            date = LocalDate.parse(dateCell.getStringCellValue());
          } catch (Exception e) {
            throw new ExcelValidationException("행 " + rowNumber + "의 날짜 형식이 유효하지 않습니다.");
          }
        } else {
          throw new ExcelValidationException("행 " + rowNumber + "의 날짜 형식이 유효하지 않습니다.");
        }
      }

      BigDecimal depWdPrice = null;
      if (depWdPriceCell != null) {
        if (depWdPriceCell.getCellType() == CellType.NUMERIC) {
          depWdPrice = BigDecimal.valueOf(depWdPriceCell.getNumericCellValue());
        } else {
          throw new ExcelValidationException("행 " + rowNumber + "의 입출금 금액이 유효한 숫자가 아닙니다.");
        }
      }

      BigDecimal dailyProfitLoss = null;
      if (dailyProfitLossCell != null) {
        if (dailyProfitLossCell.getCellType() == CellType.NUMERIC) {
          dailyProfitLoss = BigDecimal.valueOf(dailyProfitLossCell.getNumericCellValue());
        } else {
          throw new ExcelValidationException("행 " + rowNumber + "의 일손익 금액이 유효한 숫자가 아닙니다.");
        }
      }

      return DailyStatisticsReqDto.builder()
              .date(date)
              .depWdPrice(depWdPrice)
              .dailyProfitLoss(dailyProfitLoss)
              .build();
    } catch (ExcelValidationException e) {
      throw e; // 커스텀 예외는 그대로 던짐
    } catch (Exception e) {
      throw new ExcelValidationException("행 " + rowNumber + "을(를) 파싱하는 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * DTO의 Bean Validation 수행
   *
   * @param dto       DTO 객체
   * @param rowNumber 행 번호 (오류 메시지용)
   */
  private void validateDto(DailyStatisticsReqDto dto, int rowNumber) {
    Set<ConstraintViolation<DailyStatisticsReqDto>> violations = validator.validate(dto);
    if (!violations.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("행 ").append(rowNumber).append("의 유효성 검사 실패: ");
      for (ConstraintViolation<DailyStatisticsReqDto> violation : violations) {
        sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
      }
      throw new ExcelValidationException(sb.toString());
    }
  }
}
