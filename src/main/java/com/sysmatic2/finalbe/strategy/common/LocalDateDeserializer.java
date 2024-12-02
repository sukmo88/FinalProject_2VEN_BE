package com.sysmatic2.finalbe.strategy.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String date = parser.getText();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                // LocalDate.parse는 유효하지 않은 날짜에서 예외를 던짐
                LocalDate parsedDate = LocalDate.parse(date, formatter);

                // 포맷된 값이 입력값과 동일한지 확인 (2024-1-2가 2024-01-02로 해석되는 경우 방지)
                if (!parsedDate.format(formatter).equals(date)) {
                    continue;
                }

                // 공휴일 및 주말 체크
                if (com.sysmatic2.finalbe.strategy.common.HolidayUtil.isHolidayOrWeekend(parsedDate)) {
                    throw new IllegalArgumentException("공휴일 또는 주말은 허용되지 않는 날짜입니다.");
                }

                // 미래 날짜 제한
                if (parsedDate.isAfter(LocalDate.now())) {
                    throw new IllegalArgumentException("미래 날짜는 허용되지 않습니다.");
                }

                return parsedDate; // 유효한 날짜 반환
            } catch (Exception ignored) {
                // 포맷 불일치 예외 무시
            }
        }
        throw new IllegalArgumentException("지원하지 않는 날짜 형식입니다. (허용 형식: yyyyMMdd, yyyy-MM-dd, yyyy/MM/dd)");
    }
}