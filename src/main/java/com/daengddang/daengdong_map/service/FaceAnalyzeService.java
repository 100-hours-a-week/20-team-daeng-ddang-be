package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.expression.Expression;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.dto.request.expression.FaceAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.expression.FastApiFaceAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.expression.FaceAnalyzeResponse;
import com.daengddang.daengdong_map.dto.response.expression.FastApiFaceAnalyzeResponse;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.ExpressionRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceAnalyzeService {

    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final WalkRepository walkRepository;
    private final ExpressionRepository expressionRepository;
    private final FastApiClient fastApiClient;

    @Transactional
    public FaceAnalyzeResponse analyze(Long userId, Long walkId, FaceAnalyzeRequest request) {

        log.info("표정분석 서비스!");
        if (request == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Dog dog = dogRepository.findByUser(user)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        Walk walk = walkRepository.findByIdAndDog(walkId, dog)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        String analysisId = UUID.randomUUID().toString();
        FastApiFaceAnalyzeRequest fastApiRequest =
                FastApiFaceAnalyzeRequest.of(analysisId, request.getVideoUrl());
        FastApiFaceAnalyzeResponse fastApiResponse =
                fastApiClient.requestFaceAnalyze(fastApiRequest);

        Expression expression = expressionRepository.findByWalk(walk)
                .orElseGet(() -> toExpression(
                        fastApiResponse,
                        request.getVideoUrl(),
                        dog,
                        walk
                ));

        if (expression.getId() == null) {
            expressionRepository.save(expression);
        } else {
            updateExpression(expression, fastApiResponse, request.getVideoUrl());
        }

        return toResponse(fastApiResponse, analysisId);
    }

    private Expression toExpression(
            FastApiFaceAnalyzeResponse response,
            String videoUrl,
            Dog dog,
            Walk walk
    ) {
        if (response == null
                || response.getEmotionProbabilities() == null
                || response.getSummary() == null
                || response.getPredictedEmotion() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        FastApiFaceAnalyzeResponse.EmotionProbabilities probabilities =
                response.getEmotionProbabilities();

        return Expression.builder()
                .videoUrl(videoUrl)
                .summary(response.getSummary())
                .predictedEmotion(response.getPredictedEmotion())
                .angry(probabilities.getAngry())
                .happy(probabilities.getHappy())
                .sad(probabilities.getSad())
                .relaxed(probabilities.getRelaxed())
                .dog(dog)
                .walk(walk)
                .build();
    }

    private void updateExpression(Expression expression,
                                  FastApiFaceAnalyzeResponse response,
                                  String videoUrl) {
        FastApiFaceAnalyzeResponse.EmotionProbabilities probabilities =
                response.getEmotionProbabilities();
        expression.updateResult(
                videoUrl,
                response.getSummary(),
                response.getPredictedEmotion(),
                probabilities.getAngry(),
                probabilities.getHappy(),
                probabilities.getSad(),
                probabilities.getRelaxed()
        );
    }

    private FaceAnalyzeResponse toResponse(FastApiFaceAnalyzeResponse response, String fallbackAnalysisId) {
        if (response == null || response.getEmotionProbabilities() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        FastApiFaceAnalyzeResponse.EmotionProbabilities probabilities =
                response.getEmotionProbabilities();

        FaceAnalyzeResponse.EmotionProbabilities responseProbabilities =
                FaceAnalyzeResponse.EmotionProbabilities.from(
                        probabilities.getAngry(),
                        probabilities.getHappy(),
                        probabilities.getSad(),
                        probabilities.getRelaxed()
                );

        String analysisId = response.getAnalysisId() == null
                ? fallbackAnalysisId
                : response.getAnalysisId();

        return FaceAnalyzeResponse.from(
                analysisId,
                response.getPredictedEmotion(),
                response.getVideoUrl(),
                response.getSummary(),
                responseProbabilities
        );
    }
}
