package com.sysmatic2.finalbe.strategy.service;

import com.sysmatic2.finalbe.strategy.common.HolidayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyStatisticsUpdateService {
    private final DailyStatisticsService dailyStatisticsService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void scheduleDailySmScoreUpdate() {
        LocalDate today = LocalDate.now();

        // 공휴일 또는 주말에 실행하지 않음
        if (HolidayUtil.isHolidayOrWeekend(today.minusDays(1))) {
            System.out.println("Scheduler skipped: 공휴일 또는 주말이므로 실행하지 않습니다. Date: " + today.minusDays(1));
            return;
        }

        System.out.println("Scheduler started: scheduleDailySmScoreUpdate");
        dailyStatisticsService.updateSmScoreInDailyStatistics();
    }
}