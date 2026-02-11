package com.daengddang.daengdong_map.dto.response.ranking.personal;

import java.util.List;
import lombok.Getter;

@Getter
public class PersonalRankingSummaryResponse {

    private final List<PersonalRankItemResponse> topRanks;
    private final PersonalRankItemResponse myRank;

    private PersonalRankingSummaryResponse(List<PersonalRankItemResponse> topRanks,
                                           PersonalRankItemResponse myRank) {
        this.topRanks = topRanks;
        this.myRank = myRank;
    }

    public static PersonalRankingSummaryResponse of(List<PersonalRankItemResponse> topRanks,
                                                    PersonalRankItemResponse myRank) {
        return new PersonalRankingSummaryResponse(topRanks, myRank);
    }
}
