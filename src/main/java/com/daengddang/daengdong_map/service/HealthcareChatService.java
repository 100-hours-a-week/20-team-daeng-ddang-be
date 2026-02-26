package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.ai.FastApiClient;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.dto.request.chat.HealthcareChatRequest;
import com.daengddang.daengdong_map.dto.response.chat.FastApiHealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatSessionResponse;
import com.daengddang.daengdong_map.util.HealthcareChatContextBuilder;
import com.daengddang.daengdong_map.service.chat.session.ChatSession;
import com.daengddang.daengdong_map.service.chat.session.ChatSessionStore;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthcareChatService {

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final AccessValidator accessValidator;
    private final FastApiClient fastApiClient;
    private final ChatSessionStore chatSessionStore;
    private final HealthcareChatContextBuilder healthcareChatContextBuilder;

    public HealthcareChatSessionResponse createSession(Long userId) {
        Dog dog = accessValidator.getDogOrThrow(userId);
        ChatSession session = chatSessionStore.create(userId, dog.getId(), SESSION_TTL);

        return HealthcareChatSessionResponse.of(
                session.getConversationId(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
    }

    public HealthcareChatResponse sendMessage(Long userId, HealthcareChatRequest request) {
        Dog dog = accessValidator.getDogOrThrow(userId);
        ChatSession session = validateSession(userId, dog.getId(), request.getConversationId());

        FastApiHealthcareChatResponse fastApiResponse = fastApiClient.requestHealthcareChat(
                healthcareChatContextBuilder.buildFastApiRequest(dog, request, session)
        );
        validateFastApiResponse(fastApiResponse, session, request.getConversationId());

        chatSessionStore.extend(session.getConversationId(), SESSION_TTL);

        return HealthcareChatResponse.of(
                fastApiResponse.getConversationId(),
                fastApiResponse.getAnsweredAt(),
                fastApiResponse.getAnswer()
        );
    }

    private ChatSession validateSession(Long userId, Long dogId, String conversationId) {
        ChatSession session = chatSessionStore.find(conversationId)
                .orElseThrow(() -> new BaseException(ErrorCode.SESSION_EXPIRED));

        if (!session.getUserId().equals(userId) || !session.getDogId().equals(dogId)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        return session;
    }

    private void validateFastApiResponse(FastApiHealthcareChatResponse response,
                                         ChatSession session,
                                         String requestedConversationId) {
        if (response != null
                && response.getErrorCode() != null
                && !response.getErrorCode().isBlank()) {
            throw new BaseException(ErrorCode.AI_SERVER_CONNECTION_FAILED);
        }

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
}
