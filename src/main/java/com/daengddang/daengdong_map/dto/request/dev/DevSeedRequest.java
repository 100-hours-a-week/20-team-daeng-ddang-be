package com.daengddang.daengdong_map.dto.request.dev;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class DevSeedRequest {

    @Min(1)
    @Max(20000)
    private Integer count;

    private String prefix;
}
