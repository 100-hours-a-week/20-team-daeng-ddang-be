package com.daengddang.daengdong_map.dto.response.user;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserSummaryResponse {

    private final String region;
    private final Long dogId;
    private final String profileImageUrl;
    private final String name;
    private final Integer totalWalkCount;
    private final Double totalWalkDistanceKm;

    @Builder
    private UserSummaryResponse(
            String region,
            Long dogId,
            String profileImageUrl,
            String name,
            Integer totalWalkCount,
            Double totalWalkDistanceKm
    ) {
        this.region = region;
        this.dogId = dogId;
        this.profileImageUrl = profileImageUrl;
        this.name = name;
        this.totalWalkCount = totalWalkCount;
        this.totalWalkDistanceKm = totalWalkDistanceKm;
    }

    public static UserSummaryResponse of(
            String region,
            Long dogId,
            String profileImageUrl,
            String name,
            Integer totalWalkCount,
            Double totalWalkDistanceKm
    ) {
        return UserSummaryResponse.builder()
                .region(region)
                .dogId(dogId)
                .profileImageUrl(profileImageUrl)
                .name(name)
                .totalWalkCount(totalWalkCount)
                .totalWalkDistanceKm(totalWalkDistanceKm)
                .build();
    }
}
