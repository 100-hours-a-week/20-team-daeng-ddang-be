package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import java.time.DateTimeException;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RankingValidator {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private static final Pattern MONTH_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})$");
    private static final Pattern WEEK_PATTERN = Pattern.compile("^(\\d{4})-W(\\d{2})$");
    private static final Pattern RANK_DOG_CURSOR_PATTERN = Pattern.compile("^rank:(\\d+),dogId:(\\d+)$");
    private static final Pattern RANK_REGION_CURSOR_PATTERN = Pattern.compile("^rank:(\\d+),regionId:(\\d+)$");

    private RankingValidator() {
    }

    public static RankingPeriodType parsePeriodType(String periodType) {
        if (periodType == null || periodType.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
        try {
            return RankingPeriodType.valueOf(periodType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
    }

    public static void validatePeriodValue(RankingPeriodType periodType, String periodValue) {
        if (periodValue == null || periodValue.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }

        switch (periodType) {
            case YEAR -> validateYear(periodValue);
            case MONTH -> validateMonth(periodValue);
            case WEEK -> validateWeek(periodValue);
            default -> throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
    }

    public static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        return limit;
    }

    public static RankDogCursor parseRankDogCursor(String cursor) {
        Matcher matcher = RANK_DOG_CURSOR_PATTERN.matcher(cursor == null ? "" : cursor.trim());
        if (!matcher.matches()) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        return new RankDogCursor(Integer.parseInt(matcher.group(1)), Long.parseLong(matcher.group(2)));
    }

    public static RankRegionCursor parseRankRegionCursor(String cursor) {
        Matcher matcher = RANK_REGION_CURSOR_PATTERN.matcher(cursor == null ? "" : cursor.trim());
        if (!matcher.matches()) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        return new RankRegionCursor(Integer.parseInt(matcher.group(1)), Long.parseLong(matcher.group(2)));
    }

    private static void validateYear(String value) {
        if (!value.matches("^\\d{4}$")) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
        try {
            Year.parse(value);
        } catch (DateTimeException e) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
    }

    private static void validateMonth(String value) {
        Matcher matcher = MONTH_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
        try {
            YearMonth.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        } catch (DateTimeException | NumberFormatException e) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
    }

    private static void validateWeek(String value) {
        Matcher matcher = WEEK_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
        try {
            int year = Integer.parseInt(matcher.group(1));
            int week = Integer.parseInt(matcher.group(2));
            int maxWeek = Year.of(year)
                    .atMonth(12)
                    .atEndOfMonth()
                    .get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            ValueRange validRange = WeekFields.ISO.weekOfWeekBasedYear().range();
            if (!validRange.isValidIntValue(week)) {
                throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
            }
            if (week > maxWeek) {
                throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
            }
        } catch (DateTimeException | NumberFormatException e) {
            throw new BaseException(ErrorCode.INVALID_PERIOD_FORMAT);
        }
    }

    public record RankDogCursor(int rank, long dogId) {
    }

    public record RankRegionCursor(int rank, long regionId) {
    }
}
