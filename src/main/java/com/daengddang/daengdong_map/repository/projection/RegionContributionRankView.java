package com.daengddang.daengdong_map.repository.projection;

public interface RegionContributionRankView {

    Integer getRank();

    Long getDogId();

    String getDogName();

    String getProfileImageUrl();

    Double getDogDistanceMeters();

    Double getContributionRate();
}
