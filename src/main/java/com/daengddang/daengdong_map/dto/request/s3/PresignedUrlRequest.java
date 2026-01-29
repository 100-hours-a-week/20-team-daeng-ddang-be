package com.daengddang.daengdong_map.dto.request.s3;

import com.daengddang.daengdong_map.domain.s3.FileType;
import com.daengddang.daengdong_map.domain.s3.UploadContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PresignedUrlRequest {

    @NotNull
    private FileType fileType;

    @NotBlank
    private String contentType;

    @NotNull
    private UploadContext uploadContext;
}
