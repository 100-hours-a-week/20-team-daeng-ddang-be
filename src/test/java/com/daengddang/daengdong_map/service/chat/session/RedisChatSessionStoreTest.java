package com.daengddang.daengdong_map.service.chat.session;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daengddang.daengdong_map.service.chat.history.ChatHistoryStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class RedisChatSessionStoreTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ChatHistoryStore chatHistoryStore;

    @Mock
    private RKeys keys;

    @Test
    void remove_deletesHistoryAndSessionKey() {
        RedisChatSessionStore store = new RedisChatSessionStore(redissonClient, chatHistoryStore);
        when(redissonClient.getKeys()).thenReturn(keys);

        store.remove("vet_sess_abc");

        verify(chatHistoryStore).remove("vet_sess_abc");
        verify(keys).delete("chat:session:vet_sess_abc");
    }
}
