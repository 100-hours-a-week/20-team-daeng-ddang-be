package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.s3.FileType;
import com.daengddang.daengdong_map.dto.request.s3.PresignedUrlRequest;
import com.daengddang.daengdong_map.dto.response.s3.PresignedUrlResponse;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private static final Duration PRESIGNED_URL_DURATION = Duration.ofSeconds(300);
    private static final DateTimeFormatter DATE_PATH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Presigner s3Presigner;
    private final AccessValidator accessValidator;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Transactional(readOnly = true)
    public PresignedUrlResponse issuePresignedUrl(Long userId, PresignedUrlRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        accessValidator.getUserOrThrow(userId);

        String contentType = dto.getContentType().trim();
        validateContentType(dto.getFileType(), contentType);

        String extension = resolveExtension(contentType);
        if (extension == null) {
            throw new BaseException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        }

        String objectKey = buildObjectKey(
                dto.getUploadContext().name().toLowerCase(Locale.ROOT),
                extension
        );

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_DURATION)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        return PresignedUrlResponse.from(
                presignedRequest.url().toString(),
                objectKey,
                PRESIGNED_URL_DURATION.getSeconds()
        );
    }

    private void validateContentType(FileType fileType, String contentType) {
        if (fileType == FileType.IMAGE && !contentType.startsWith("image/")) {
            throw new BaseException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        }
        if (fileType == FileType.VIDEO && !contentType.startsWith("video/")) {
            throw new BaseException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        }
    }

    private String resolveExtension(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT);
        int slashIndex = normalized.indexOf('/');
        if (slashIndex < 0 || slashIndex == normalized.length() - 1) {
            return null;
        }

        String subtype = normalized.substring(slashIndex + 1);
        if (subtype.startsWith("jpeg") || subtype.startsWith("jpg")) {
            return "jpg";
        }
        if (subtype.startsWith("png")) {
            return "png";
        }
        if (subtype.startsWith("webp")) {
            return "webp";
        }
        if (subtype.startsWith("gif")) {
            return "gif";
        }
        if (subtype.startsWith("heic")) {
            return "heic";
        }
        if (subtype.startsWith("heif")) {
            return "heif";
        }
        if (subtype.startsWith("mp4")) {
            return "mp4";
        }
        if (subtype.startsWith("quicktime")) {
            return "mov";
        }
        if (subtype.startsWith("x-m4v")) {
            return "m4v";
        }
        if (subtype.startsWith("mpeg")) {
            return "mpeg";
        }

        return null;
    }

    private String buildObjectKey(String context, String extension) {
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        return context + "/" + datePath + "/uuid_" + UUID.randomUUID() + "." + extension;
    }
}
