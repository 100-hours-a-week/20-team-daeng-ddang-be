package com.daengddang.daengdong_map.dto.request.healthcare;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class HealthcareAnalyzeRequest {

    @NotBlank
    private String videoUrl;
}
