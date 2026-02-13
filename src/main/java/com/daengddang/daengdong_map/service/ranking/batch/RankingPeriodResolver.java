package com.daengddang.daengdong_map.service.ranking.batch;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import org.springframework.stereotype.Component;

@Component
public class RankingPeriodResolver {

    private static final ZoneId BATCH_ZONE = ZoneId.of("Asia/Seoul");

    public String resolveCurrentPeriodValue(RankingPeriodType periodType) {
        return resolvePeriodValue(periodType, LocalDate.now(BATCH_ZONE));
    }

    public String resolvePeriodValue(RankingPeriodType periodType, LocalDate date) {
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
}
