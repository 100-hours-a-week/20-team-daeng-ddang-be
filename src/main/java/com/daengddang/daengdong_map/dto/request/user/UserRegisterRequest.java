package com.daengddang.daengdong_map.dto.request.user;

import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserRegisterRequest {

    @NotNull
    private Long regionId;

    public static User of(UserRegisterRequest dto, User user, Region region) {
        user.updateRegion(region);
        return user;
    }
}
