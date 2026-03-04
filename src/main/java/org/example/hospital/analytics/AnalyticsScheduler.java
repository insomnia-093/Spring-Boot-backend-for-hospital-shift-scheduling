package org.example.hospital.analytics;

import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsScheduler {

    private final AnalyticsService analyticsService;

    public AnalyticsScheduler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Scheduled(cron = "${app.analytics.refresh.daily-cron:0 10 0 * * *}")
    public void refreshDaily() {
        analyticsService.refreshDaily(LocalDate.now());
    }

    @Scheduled(cron = "${app.analytics.refresh.monthly-cron:0 20 0 1 * *}")
    public void refreshMonthly() {
        analyticsService.refreshMonthly(YearMonth.now().minusMonths(1));
    }
}
