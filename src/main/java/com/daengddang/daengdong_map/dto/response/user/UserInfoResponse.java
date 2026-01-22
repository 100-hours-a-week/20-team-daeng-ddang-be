package com.daengddang.daengdong_map.dto.response.user;

import com.daengddang.daengdong_map.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfoResponse {

    private Long userId;

    private String kakaoEmail;

    private Long regionId;
    private Long parentRegionId;

    private String region;

    @Builder
    public UserInfoResponse(Long userId, String kakaoEmail, Long regionId, Long parentRegionId, String region) {
        this.userId = userId;
        this.kakaoEmail = kakaoEmail;
        this.regionId = regionId;
        this.parentRegionId = parentRegionId;
        this.region = region;
    }

    public static UserInfoResponse from(User user) {

        if (user.getRegion() == null) {
            return UserInfoResponse.builder()
                    .userId(user.getId())
                    .regionId(null)
                    .parentRegionId(null)
                    .region(null)
                    .build();
        }

        return UserInfoResponse.builder()
                .userId(user.getId())
                .kakaoEmail(user.getKakaoEmail())
                .regionId(user.getRegion().getId())
                .parentRegionId(
                        user.getRegion().getParent() != null
                                ? user.getRegion().getParent().getId()
                                : null
                )
                .region(user.getRegion().getFullName())
                .build();
    }
}
