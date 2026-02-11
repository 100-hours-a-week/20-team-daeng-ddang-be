package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.diary.WalkDiary;
import com.daengddang.daengdong_map.dto.request.footprint.FootprintCalendarRequest;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintCalendarDayResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintCalendarResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintDiaryDetailResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintDiaryExpressionResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintDailyRecordItemResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintDailyRecordsResponse;
import com.daengddang.daengdong_map.dto.response.footprint.FootprintRecordType;
import com.daengddang.daengdong_map.repository.AnalysisRepository;
import com.daengddang.daengdong_map.repository.ExpressionRepository;
import com.daengddang.daengdong_map.repository.WalkDiaryRepository;
import com.daengddang.daengdong_map.repository.projection.DailyRecordView;
import com.daengddang.daengdong_map.repository.projection.DateCountView;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FootprintService {
    private static final String WALK_TITLE_SUFFIX = " 산책일지";
    private static final String HEALTH_TITLE_SUFFIX = " 헬스케어";
    private static final DateTimeFormatter TITLE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private final WalkDiaryRepository walkDiaryRepository;
    private final AnalysisRepository analysisRepository;
    private final ExpressionRepository expressionRepository;
    private final AccessValidator accessValidator;

    public FootprintCalendarResponse getCalendarRecords(Long userId, FootprintCalendarRequest request) {
        accessValidator.getUserOrThrow(userId);

        YearMonth yearMonth = toYearMonth(request.getYear(), request.getMonth());
        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endAt = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<DateCountView> walkDiaryCounts = walkDiaryRepository.countWalkDiaryByDate(userId, startAt, endAt);
        List<DateCountView> healthCareCounts = analysisRepository.countHealthCareByDate(userId, startAt, endAt);

        Map<LocalDate, int[]> dayCountMap = new TreeMap<>();
        mergeCounts(dayCountMap, walkDiaryCounts, 0);
        mergeCounts(dayCountMap, healthCareCounts, 1);

        List<FootprintCalendarDayResponse> days = new ArrayList<>();
        for (Map.Entry<LocalDate, int[]> entry : dayCountMap.entrySet()) {
            int walkDiaryCount = entry.getValue()[0];
            int healthCareCount = entry.getValue()[1];
            days.add(FootprintCalendarDayResponse.of(entry.getKey(), walkDiaryCount, healthCareCount));
        }

        return FootprintCalendarResponse.of(yearMonth.getYear(), yearMonth.getMonthValue(), days);
    }

    public FootprintDailyRecordsResponse getDailyRecords(Long userId, String dateText) {
        accessValidator.getUserOrThrow(userId);

        LocalDate date = parseDate(dateText);
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime endAt = date.plusDays(1).atStartOfDay();

        List<DailyRecordView> walkRecords = walkDiaryRepository.findDailyWalkRecords(userId, startAt, endAt);
        List<DailyRecordView> healthRecords = analysisRepository.findDailyHealthRecords(userId, startAt, endAt);

        List<DailyRecordWithTime> merged = new ArrayList<>();
        for (DailyRecordView walkRecord : walkRecords) {
            String title = dateTimeToTitlePrefix(walkRecord.getCreatedAt()) + WALK_TITLE_SUFFIX;
            merged.add(new DailyRecordWithTime(
                    walkRecord.getCreatedAt(),
                    FootprintDailyRecordItemResponse.of(
                            FootprintRecordType.WALK,
                            walkRecord.getId(),
                            title,
                            walkRecord.getCreatedAt()
                    )
            ));
        }

        for (DailyRecordView healthRecord : healthRecords) {
            String title = dateTimeToTitlePrefix(healthRecord.getCreatedAt()) + HEALTH_TITLE_SUFFIX;
            merged.add(new DailyRecordWithTime(
                    healthRecord.getCreatedAt(),
                    FootprintDailyRecordItemResponse.of(
                            FootprintRecordType.HEALTH,
                            healthRecord.getId(),
                            title,
                            healthRecord.getCreatedAt()
                    )
            ));
        }

        merged.sort(Comparator.comparing(DailyRecordWithTime::createdAt));

        List<FootprintDailyRecordItemResponse> records = new ArrayList<>();
        for (DailyRecordWithTime item : merged) {
            records.add(item.record());
        }

        return FootprintDailyRecordsResponse.of(date, records);
    }

    public FootprintDiaryExpressionResponse getDiaryExpression(Long userId, Long walkDiaryId) {
        accessValidator.getUserOrThrow(userId);

        WalkDiary walkDiary = walkDiaryRepository.findByIdWithUserRegionAndWalk(walkDiaryId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!userId.equals(walkDiary.getUser().getId())) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        return expressionRepository.findByWalk(walkDiary.getWalk())
                .map(FootprintDiaryExpressionResponse::from)
                .orElse(null);
    }

    public FootprintDiaryDetailResponse getDiaryDetail(Long userId, Long walkDiaryId) {
        accessValidator.getUserOrThrow(userId);

        WalkDiary walkDiary = walkDiaryRepository.findByIdWithUserRegionAndWalk(walkDiaryId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!userId.equals(walkDiary.getUser().getId())) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        String region = walkDiary.getUser().getRegion() == null
                ? null
                : walkDiary.getUser().getRegion().getFullName();

        return FootprintDiaryDetailResponse.of(
                walkDiary.getId(),
                walkDiary.getCreatedAt(),
                walkDiary.getMapImageUrl(),
                walkDiary.getWalk().getDistance(),
                walkDiary.getWalk().getDuration(),
                region,
                walkDiary.getMemo()
        );
    }

    private YearMonth toYearMonth(Integer year, Integer month) {
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException | NullPointerException e) {
            throw new BaseException(ErrorCode.INVALID_DATE_REQUEST);
        }
    }

    private LocalDate parseDate(String dateText) {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeException | NullPointerException e) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    private String dateTimeToTitlePrefix(LocalDateTime dateTime) {
        return dateTime.format(TITLE_DATE_TIME_FORMATTER);
    }

    private void mergeCounts(Map<LocalDate, int[]> dayCountMap, List<DateCountView> counts, int typeIndex) {
        for (DateCountView countView : counts) {
            LocalDate date = countView.getDate();
            int count = Math.toIntExact(countView.getCount() == null ? 0L : countView.getCount());
            dayCountMap.computeIfAbsent(date, ignored -> new int[2])[typeIndex] = count;
        }
    }

    private record DailyRecordWithTime(LocalDateTime createdAt, FootprintDailyRecordItemResponse record) {
    }
}
