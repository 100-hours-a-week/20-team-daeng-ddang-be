package com.daengddang.daengdong_map.service.chat.history;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class RedisChatHistoryStore implements ChatHistoryStore {

    private static final String KEY_PREFIX = "chat:history:";

    private final RedissonClient redissonClient;

    public RedisChatHistoryStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public List<ChatHistoryMessage> findAll(String conversationId) {
        RList<ChatHistoryMessage> list = redissonClient.getList(key(conversationId));
        return new ArrayList<>(list.readAll());
    }

    @Override
    public void append(String conversationId, ChatHistoryMessage message, Duration ttl) {
        RList<ChatHistoryMessage> list = redissonClient.getList(key(conversationId));
        list.add(message);
        list.expire(ttl);
    }

    @Override
    public void remove(String conversationId) {
        redissonClient.getKeys().delete(key(conversationId));
    }

    private String key(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
