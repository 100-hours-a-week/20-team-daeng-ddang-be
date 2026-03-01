package com.daengddang.daengdong_map.dto.response.ranking.personal;

import java.util.List;
import lombok.Getter;

@Getter
public class PersonalRankingListResponse {

    private final List<PersonalRankItemResponse> ranks;
    private final String nextCursor;
    private final boolean hasNext;

    private PersonalRankingListResponse(List<PersonalRankItemResponse> ranks, String nextCursor, boolean hasNext) {
        this.ranks = ranks;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static PersonalRankingListResponse of(List<PersonalRankItemResponse> ranks, String nextCursor, boolean hasNext) {
        return new PersonalRankingListResponse(ranks, nextCursor, hasNext);
    }
}
