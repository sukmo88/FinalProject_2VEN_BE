package com.sysmatic2.finalbe.strategy.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HolidayUtil {

    // 고정된 공휴일 목록 (MM-dd 형식)
    private static final Set<String> FIXED_HOLIDAYS = new HashSet<>(Arrays.asList(
            "01-01", // 새해 첫날
            "03-01", // 삼일절
            "05-05", // 어린이날
            "08-15", // 광복절
            "10-03", // 개천절
            "12-25"  // 크리스마스
    ));

    /**
     * 특정 날짜가 공휴일인지 확인
     *
     * @param date 확인할 날짜
     * @return 공휴일 여부
     */
    public static boolean isHoliday(LocalDate date) {
        String monthDay = date.format(DateTimeFormatter.ofPattern("MM-dd"));
        return FIXED_HOLIDAYS.contains(monthDay);
    }

    /**
     * 특정 날짜가 주말인지 확인
     *
     * @param date 확인할 날짜
     * @return 주말 여부
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 특정 날짜가 공휴일 또는 주말인지 확인
     *
     * @param date 확인할 날짜
     * @return 공휴일 또는 주말 여부
     */
    public static boolean isHolidayOrWeekend(LocalDate date) {
        return isHoliday(date) || isWeekend(date);
    }
}