package com.daengddang.daengdong_map.service.chat.session;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class RedisChatSessionStore implements ChatSessionStore {

    private static final String SESSION_ID_PREFIX = "vet_sess_";
    private static final String KEY_PREFIX = "chat:session:";

    private final RedissonClient redissonClient;

    public RedisChatSessionStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public ChatSession create(Long userId, Long dogId, Duration ttl) {
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = createdAt.plus(ttl);
        String conversationId = SESSION_ID_PREFIX + UUID.randomUUID().toString().replace("-", "");

        RMapCache<String, String> map = redissonClient.getMapCache(key(conversationId));
        map.fastPut("conversationId", conversationId);
        map.fastPut("userId", String.valueOf(userId));
        map.fastPut("dogId", String.valueOf(dogId));
        map.fastPut("createdAt", createdAt.toString());
        map.fastPut("expiresAt", expiresAt.toString());
        map.expire(ttl);

        return new ChatSession(conversationId, userId, dogId, createdAt, expiresAt);
    }

    @Override
    public Optional<ChatSession> find(String conversationId) {
        RMapCache<String, String> map = redissonClient.getMapCache(key(conversationId));

        String userId = map.get("userId");
        String dogId = map.get("dogId");
        String createdAt = map.get("createdAt");
        String expiresAt = map.get("expiresAt");
        if (userId == null || dogId == null || createdAt == null || expiresAt == null) {
            return Optional.empty();
        }

        return Optional.of(new ChatSession(
                conversationId,
                Long.parseLong(userId),
                Long.parseLong(dogId),
                OffsetDateTime.parse(createdAt),
                OffsetDateTime.parse(expiresAt)
        ));
    }

    @Override
    public void extend(String conversationId, Duration ttl) {
        RMapCache<String, String> map = redissonClient.getMapCache(key(conversationId));
        String userId = map.get("userId");
        if (userId == null) {
            return;
        }
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(ttl);
        map.fastPut("expiresAt", expiresAt.toString());
        map.expire(ttl);
    }

    @Override
    public void remove(String conversationId) {
        redissonClient.getKeys().delete(key(conversationId));
    }

    private String key(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
