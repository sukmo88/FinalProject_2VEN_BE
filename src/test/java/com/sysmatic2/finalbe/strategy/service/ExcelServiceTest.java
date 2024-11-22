package com.sysmatic2.finalbe.strategy.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ExcelService.class})
public class ExcelServiceTest {

  @Autowired
  private ExcelService excelService;

  @Test
  public void testExtractData() throws Exception {
    // Given: 테스트용 엑셀 파일 준비
    String absolutePath = "C:/Users/USER/Desktop/파이널 프로젝트/FinalProject_2VEN_BE/src/test/java/resources/test-files/sample.xlsx";
    File file = new File(absolutePath);
    FileInputStream inputStream = new FileInputStream(file);

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file",
            "sample.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            inputStream
    );

    // When: Extract data from Excel
    List<List<Object>> data = excelService.extractData(mockMultipartFile);

    // Then: Verify the extracted data
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(4); // Header + 3 rows

    // Verify header
    List<Object> header = data.get(0);
    assertThat(header.get(0)).isEqualTo("날짜");
    assertThat(header.get(1)).isEqualTo("금액");
    assertThat(header.get(2)).isEqualTo("수익률");

    // Verify first transaction (2024-11-01)
    List<Object> row1 = data.get(1);
    assertThat(row1.get(0)).isEqualTo("2024-11-01"); // 날짜
    assertThat(row1.get(1)).isEqualTo(100000.0);     // 금액 (입금)
    assertThat(row1.get(2)).isEqualTo(0.05);         // 수익률

    // Verify second transaction (2024-11-02)
    List<Object> row2 = data.get(2);
    assertThat(row2.get(0)).isEqualTo("2024-11-02"); // 날짜
    assertThat(row2.get(1)).isEqualTo(-20000.0);     // 금액 (출금)
    assertThat(row2.get(2)).isEqualTo(0.03);         // 수익률

    // Verify third transaction (2024-11-03)
    List<Object> row3 = data.get(3);
    assertThat(row3.get(0)).isEqualTo("2024-11-03"); // 날짜
    assertThat(row3.get(1)).isEqualTo(-100000.0);    // 금액 (출금)
    assertThat(row3.get(2)).isEqualTo(-0.02);        // 수익률
  }
}
