package com.daengddang.daengdong_map.dto.request.expression;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FaceAnalyzeRequest {

    @NotBlank
    private String videoUrl;
}
