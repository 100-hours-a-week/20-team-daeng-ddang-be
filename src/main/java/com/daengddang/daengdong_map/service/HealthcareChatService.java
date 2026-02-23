package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.ai.FastApiClient;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.dto.request.chat.FastApiHealthcareChatRequest;
import com.daengddang.daengdong_map.dto.request.chat.HealthcareChatRequest;
import com.daengddang.daengdong_map.dto.response.chat.FastApiHealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatSessionResponse;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthcareChatService {

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final AccessValidator accessValidator;
    private final FastApiClient fastApiClient;

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    public HealthcareChatSessionResponse createSession(Long userId) {
        Dog dog = accessValidator.getDogOrThrow(userId);
        cleanupExpiredSessions();

        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = createdAt.plus(SESSION_TTL);
        String conversationId = "vet_sess_" + UUID.randomUUID().toString().replace("-", "");

        ChatSession session = new ChatSession(
                conversationId,
                userId,
                dog.getId(),
                createdAt,
                expiresAt
        );
        sessions.put(conversationId, session);

        return HealthcareChatSessionResponse.of(conversationId, createdAt, expiresAt);
    }

    public HealthcareChatResponse sendMessage(Long userId, HealthcareChatRequest request) {
        Dog dog = accessValidator.getDogOrThrow(userId);
        cleanupExpiredSessions();

        ChatSession session = validateSession(userId, dog.getId(), request.getConversationId());

        FastApiHealthcareChatRequest fastApiRequest = buildFastApiRequest(dog, request, session);
        FastApiHealthcareChatResponse fastApiResponse = fastApiClient.requestHealthcareChat(fastApiRequest);
        validateFastApiResponse(fastApiResponse, session, request.getConversationId());

        session.touch(OffsetDateTime.now(ZoneOffset.UTC).plus(SESSION_TTL));

        return HealthcareChatResponse.of(
                fastApiResponse.getConversationId(),
                fastApiResponse.getAnsweredAt(),
                fastApiResponse.getAnswer()
        );
    }

    private FastApiHealthcareChatRequest buildFastApiRequest(Dog dog,
                                                             HealthcareChatRequest request,
                                                             ChatSession session) {
        Integer dogAgeYears = calculateDogAgeYears(dog);
        Integer dogWeightKg = toWeightKg(dog.getWeight());
        String breedName = dog.getBreed() == null ? null : dog.getBreed().getName();

        FastApiHealthcareChatRequest.UserContext userContext =
                FastApiHealthcareChatRequest.UserContext.of(dogAgeYears, dogWeightKg, breedName);

        return FastApiHealthcareChatRequest.of(
                Math.toIntExact(session.getDogId()),
                session.getConversationId(),
                request.getMessage(),
                request.getImageUrl(),
                userContext,
                Collections.emptyList()
        );
    }

    private void validateFastApiResponse(FastApiHealthcareChatResponse response,
                                         ChatSession session,
                                         String requestedConversationId) {
        if (response == null
                || response.getDogId() == null
                || response.getConversationId() == null
                || response.getAnsweredAt() == null
                || response.getAnswer() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        if (!requestedConversationId.equals(response.getConversationId())
                || !session.getConversationId().equals(response.getConversationId())
                || response.getDogId().longValue() != session.getDogId()) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    private ChatSession validateSession(Long userId, Long dogId, String conversationId) {
        ChatSession session = sessions.get(conversationId);
        if (session == null) {
            throw new BaseException(ErrorCode.SESSION_EXPIRED);
        }

        if (OffsetDateTime.now(ZoneOffset.UTC).isAfter(session.getExpiresAt())) {
            sessions.remove(conversationId);
            throw new BaseException(ErrorCode.SESSION_EXPIRED);
        }

        if (!session.getUserId().equals(userId) || !session.getDogId().equals(dogId)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        return session;
    }

    private Integer calculateDogAgeYears(Dog dog) {
        if (dog.getBirthDate() == null) {
            return 0;
        }
        return Math.max(0, Period.between(dog.getBirthDate(), LocalDate.now()).getYears());
    }

    private Integer toWeightKg(Float weight) {
        if (weight == null) {
            return 0;
        }
        return Math.round(weight);
    }

    private void cleanupExpiredSessions() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        sessions.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpiresAt()));
    }

    private static class ChatSession {
        private final String conversationId;
        private final Long userId;
        private final Long dogId;
        private final OffsetDateTime createdAt;
        private OffsetDateTime expiresAt;

        private ChatSession(String conversationId,
                            Long userId,
                            Long dogId,
                            OffsetDateTime createdAt,
                            OffsetDateTime expiresAt) {
            this.conversationId = conversationId;
            this.userId = userId;
            this.dogId = dogId;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
        }

        private String getConversationId() {
            return conversationId;
        }

        private Long getUserId() {
            return userId;
        }

        private Long getDogId() {
            return dogId;
        }

        private OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        private OffsetDateTime getExpiresAt() {
            return expiresAt;
        }

        private void touch(OffsetDateTime expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
