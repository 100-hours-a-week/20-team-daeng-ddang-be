package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.dto.request.ranking.RankingCursorRequest;
import com.daengddang.daengdong_map.dto.request.ranking.RankingPeriodRegionRequest;
import org.springframework.stereotype.Component;

@Component
public class RankingRequestValidator {

    public void validateRequestNotNull(Object request) {
        if (request == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    public void validateRequestWithRegionId(RankingPeriodRegionRequest request) {
        if (request == null || request.getRegionId() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    public RankingPeriodType parseAndValidatePeriod(String periodType, String periodValue) {
        RankingPeriodType parsedPeriodType = RankingValidator.parsePeriodType(periodType);
        RankingValidator.validatePeriodValue(parsedPeriodType, periodValue);
        return parsedPeriodType;
    }

    public int resolveLimit(RankingCursorRequest cursorDto) {
        return RankingValidator.resolveLimit(cursorDto == null ? null : cursorDto.getLimit());
    }

    public String resolveCursor(RankingCursorRequest cursorDto) {
        return cursorDto == null ? null : cursorDto.getCursor();
    }
}
