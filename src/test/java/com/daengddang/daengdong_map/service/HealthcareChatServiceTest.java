package com.daengddang.daengdong_map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daengddang.daengdong_map.ai.FastApiClient;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.chat.FastApiHealthcareChatRequest;
import com.daengddang.daengdong_map.dto.request.chat.HealthcareChatRequest;
import com.daengddang.daengdong_map.dto.response.chat.FastApiHealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatResponse;
import com.daengddang.daengdong_map.dto.response.chat.HealthcareChatSessionResponse;
import com.daengddang.daengdong_map.service.chat.session.ChatSession;
import com.daengddang.daengdong_map.service.chat.session.ChatSessionStore;
import com.daengddang.daengdong_map.util.AccessValidator;
import com.daengddang.daengdong_map.util.HealthcareChatContextBuilder;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HealthcareChatServiceTest {

    @Mock
    private AccessValidator accessValidator;

    @Mock
    private FastApiClient fastApiClient;

    @Mock
    private ChatSessionStore chatSessionStore;

    @Mock
    private HealthcareChatContextBuilder healthcareChatContextBuilder;

    @InjectMocks
    private HealthcareChatService healthcareChatService;

    @Test
    void createSession_returnsSessionResponseFromStore() {
        Dog dog = dog(10L, 1L);
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-02-23T12:00:00Z");
        OffsetDateTime expiresAt = createdAt.plusMinutes(30);
        ChatSession session = new ChatSession("vet_sess_abc", 1L, 10L, createdAt, expiresAt);

        when(accessValidator.getDogOrThrow(1L)).thenReturn(dog);
        when(chatSessionStore.create(eq(1L), eq(10L), any())).thenReturn(session);

        HealthcareChatSessionResponse response = healthcareChatService.createSession(1L);

        assertThat(response.getConversationId()).isEqualTo("vet_sess_abc");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void sendMessage_throwsWhenSessionExpired() {
        Dog dog = dog(10L, 1L);
        HealthcareChatRequest request = HealthcareChatRequest.of("vet_sess_missing", "hello", null);

        when(accessValidator.getDogOrThrow(1L)).thenReturn(dog);
        when(chatSessionStore.find("vet_sess_missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> healthcareChatService.sendMessage(1L, request))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SESSION_EXPIRED);
    }

    @Test
    void sendMessage_throwsWhenSessionOwnerMismatch() {
        Dog dog = dog(10L, 1L);
        HealthcareChatRequest request = HealthcareChatRequest.of("vet_sess_abc", "hello", null);
        ChatSession session = new ChatSession(
                "vet_sess_abc",
                2L,
                99L,
                OffsetDateTime.parse("2026-02-23T12:00:00Z"),
                OffsetDateTime.parse("2026-02-23T12:30:00Z")
        );

        when(accessValidator.getDogOrThrow(1L)).thenReturn(dog);
        when(chatSessionStore.find("vet_sess_abc")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> healthcareChatService.sendMessage(1L, request))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void sendMessage_throwsWhenFastApiResponseIsInvalid() {
        Dog dog = dog(10L, 1L);
        HealthcareChatRequest request = HealthcareChatRequest.of("vet_sess_abc", "hello", null);
        ChatSession session = new ChatSession(
                "vet_sess_abc",
                1L,
                10L,
                OffsetDateTime.parse("2026-02-23T12:00:00Z"),
                OffsetDateTime.parse("2026-02-23T12:30:00Z")
        );

        FastApiHealthcareChatRequest fastApiRequest =
                FastApiHealthcareChatRequest.of(
                        10,
                        "vet_sess_abc",
                        "hello",
                        null,
                        FastApiHealthcareChatRequest.UserContext.of(3, 7, "말티즈"),
                        java.util.Collections.emptyList()
                );
        FastApiHealthcareChatResponse invalidResponse = new FastApiHealthcareChatResponse();
        ReflectionTestUtils.setField(invalidResponse, "dogId", 10);
        ReflectionTestUtils.setField(invalidResponse, "conversationId", "vet_sess_abc");

        when(accessValidator.getDogOrThrow(1L)).thenReturn(dog);
        when(chatSessionStore.find("vet_sess_abc")).thenReturn(Optional.of(session));
        when(healthcareChatContextBuilder.buildFastApiRequest(dog, request, session)).thenReturn(fastApiRequest);
        when(fastApiClient.requestHealthcareChat(fastApiRequest)).thenReturn(invalidResponse);

        assertThatThrownBy(() -> healthcareChatService.sendMessage(1L, request))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FORMAT);
    }

    @Test
    void sendMessage_returnsResponseAndExtendsSession() {
        Dog dog = dog(10L, 1L);
        HealthcareChatRequest request = HealthcareChatRequest.of("vet_sess_abc", "hello", null);
        ChatSession session = new ChatSession(
                "vet_sess_abc",
                1L,
                10L,
                OffsetDateTime.parse("2026-02-23T12:00:00Z"),
                OffsetDateTime.parse("2026-02-23T12:30:00Z")
        );

        FastApiHealthcareChatRequest fastApiRequest =
                FastApiHealthcareChatRequest.of(
                        10,
                        "vet_sess_abc",
                        "hello",
                        null,
                        FastApiHealthcareChatRequest.UserContext.of(3, 7, "말티즈"),
                        java.util.Collections.emptyList()
                );

        FastApiHealthcareChatResponse fastApiResponse = new FastApiHealthcareChatResponse();
        ReflectionTestUtils.setField(fastApiResponse, "dogId", 10);
        ReflectionTestUtils.setField(fastApiResponse, "conversationId", "vet_sess_abc");
        ReflectionTestUtils.setField(fastApiResponse, "answeredAt", OffsetDateTime.parse("2026-02-23T12:00:10Z"));
        ReflectionTestUtils.setField(fastApiResponse, "answer", "answer");

        when(accessValidator.getDogOrThrow(1L)).thenReturn(dog);
        when(chatSessionStore.find("vet_sess_abc")).thenReturn(Optional.of(session));
        when(healthcareChatContextBuilder.buildFastApiRequest(dog, request, session)).thenReturn(fastApiRequest);
        when(fastApiClient.requestHealthcareChat(fastApiRequest)).thenReturn(fastApiResponse);

        HealthcareChatResponse response = healthcareChatService.sendMessage(1L, request);

        assertThat(response.getConversationId()).isEqualTo("vet_sess_abc");
        assertThat(response.getAnsweredAt()).isEqualTo(OffsetDateTime.parse("2026-02-23T12:00:10Z"));
        assertThat(response.getAnswer()).isEqualTo("answer");
        verify(chatSessionStore).extend(eq("vet_sess_abc"), any());
    }

    private Dog dog(Long dogId, Long userId) {
        User user = User.builder().kakaoUserId(111L).build();
        ReflectionTestUtils.setField(user, "id", userId);
        Breed breed = Breed.builder().name("말티즈").build();
        Dog dog = Dog.builder().name("coco").user(user).breed(breed).build();
        ReflectionTestUtils.setField(dog, "id", dogId);
        return dog;
    }
}
