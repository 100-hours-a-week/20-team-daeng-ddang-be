package com.daengddang.daengdong_map.dto.response.ranking.personal;

import lombok.Getter;

@Getter
public class PersonalRankItemResponse {

    private final Integer rank;
    private final Long dogId;
    private final String dogName;
    private final String profileImageUrl;
    private final Double totalDistance;

    private PersonalRankItemResponse(Integer rank,
                                     Long dogId,
                                     String dogName,
                                     String profileImageUrl,
                                     Double totalDistance) {
        this.rank = rank;
        this.dogId = dogId;
        this.dogName = dogName;
        this.profileImageUrl = profileImageUrl;
        this.totalDistance = totalDistance;
    }

    public static PersonalRankItemResponse of(Integer rank,
                                              Long dogId,
                                              String dogName,
                                              String profileImageUrl,
                                              Double totalDistance) {
        return new PersonalRankItemResponse(rank, dogId, dogName, profileImageUrl, totalDistance);
    }
}
