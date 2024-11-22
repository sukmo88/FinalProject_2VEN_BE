package com.sysmatic2.finalbe.strategy.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

  /**
   * 엑셀 파일의 데이터를 추출
   *
   * @param file 업로드된 엑셀 파일
   * @return 추출된 데이터 리스트
   */
  public List<List<Object>> extractData(MultipartFile file) {
    List<List<Object>> excelData = new ArrayList<>();
    try (InputStream inputStream = file.getInputStream();
         Workbook workbook = new XSSFWorkbook(inputStream)) {

      Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트만 처리
      for (Row row : sheet) {
        List<Object> rowData = new ArrayList<>();
        for (Cell cell : row) {
          rowData.add(getCellValue(cell)); // 셀 데이터를 타입에 맞게 추출
        }
        excelData.add(rowData);
      }
    } catch (Exception e) {
      throw new RuntimeException("엑셀 데이터 추출 중 오류가 발생했습니다.", e);
    }

    return excelData;
  }

  /**
   * 셀의 값을 타입에 따라 반환
   *
   * @param cell 엑셀 셀
   * @return 셀 데이터 (타입에 맞는 값)
   */
  private Object getCellValue(Cell cell) {
    if (cell == null) {
      return null;
    }

    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue(); // 날짜인 경우
        } else {
          return cell.getNumericCellValue(); // 숫자인 경우
        }
      case FORMULA:
        return evaluateFormula(cell); // 수식 처리
      case BOOLEAN:
        return cell.getBooleanCellValue();
      case BLANK:
        return "";
      default:
        return null; // 그 외의 경우
    }
  }

  /**
   * 수식을 평가하여 값을 반환
   *
   * @param cell 수식이 포함된 셀
   * @return 평가된 값
   */
  private Object evaluateFormula(Cell cell) {
    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
    CellValue cellValue = evaluator.evaluate(cell);

    switch (cellValue.getCellType()) {
      case STRING:
        return cellValue.getStringValue();
      case NUMERIC:
        return cellValue.getNumberValue();
      case BOOLEAN:
        return cellValue.getBooleanValue();
      default:
        return null;
    }
  }
}
