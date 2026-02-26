package com.daengddang.daengdong_map.service.ranking.batch;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Component;

@Component
public class RankingPeriodResolver {

    private static final ZoneId BATCH_ZONE = ZoneId.of("Asia/Seoul");

    public PeriodRange resolveCurrentPeriodRange(RankingPeriodType periodType) {
        return resolvePeriodRange(periodType, LocalDate.now(BATCH_ZONE));
    }

    public PeriodRange resolvePeriodRange(RankingPeriodType periodType, LocalDate date) {
        return switch (periodType) {
            case YEAR -> from(date.withDayOfYear(1), date.plusYears(1).withDayOfYear(1));
            case MONTH -> from(date.withDayOfMonth(1), date.plusMonths(1).withDayOfMonth(1));
            case WEEK -> {
                LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield from(weekStart, weekStart.plusWeeks(1));
            }
        };
    }

    public String resolvePeriodValue(RankingPeriodType periodType, PeriodRange periodRange) {
        LocalDate date = periodRange.startAt().toLocalDate();
        return switch (periodType) {
            case YEAR -> String.format("%04d", date.getYear());
            case MONTH -> String.format("%04d-%02d", date.getYear(), date.getMonthValue());
            case WEEK -> String.format(
                    "%04d-W%02d",
                    date.get(IsoFields.WEEK_BASED_YEAR),
                    date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            );
        };
    }

    private PeriodRange from(LocalDate startDate, LocalDate endDate) {
        return new PeriodRange(startDate.atStartOfDay(), endDate.atStartOfDay());
    }
}
