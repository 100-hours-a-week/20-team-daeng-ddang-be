package com.daengddang.daengdong_map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daengddang.daengdong_map.dto.websocket.common.WebSocketEventType;
import com.daengddang.daengdong_map.dto.websocket.common.WebSocketMessage;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BlockSyncServiceTest {

    @Mock
    private BlockOwnershipRepository blockOwnershipRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BlockSyncService blockSyncService;

    @BeforeEach
    void setUp() {
        when(blockOwnershipRepository.findAllByBlockRange(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
    }

    @Test
    void syncBlocksOnAreaChange_onlySyncsWhenAreaChanges() {
        LocalDateTime now = LocalDateTime.now();
        String areaKey = blockSyncService.toAreaKey(0, 0);

        blockSyncService.syncBlocksOnAreaChange(1L, 0, 0, areaKey, now);
        blockSyncService.syncBlocksOnAreaChange(1L, 1, 1, areaKey, now.plusSeconds(1));

        verifyBlocksSyncMessagesSent(1);
    }

    @Test
    void syncBlocksOnAreaChange_syncsWhenAreaChanges() {
        LocalDateTime now = LocalDateTime.now();
        String firstArea = blockSyncService.toAreaKey(0, 0);
        String nextArea = blockSyncService.toAreaKey(13, 0);

        blockSyncService.syncBlocksOnAreaChange(1L, 0, 0, firstArea, now);
        blockSyncService.syncBlocksOnAreaChange(1L, 13, 0, nextArea, now.plusSeconds(1));

        verifyBlocksSyncMessagesSent(2);
    }

    @Test
    void syncBlocks_throttlesWithinSameArea() {
        LocalDateTime now = LocalDateTime.now();
        String areaKey = blockSyncService.toAreaKey(0, 0);

        blockSyncService.syncBlocks(1L, 0, 0, areaKey, now);
        blockSyncService.syncBlocks(1L, 1, 1, areaKey, now.plusSeconds(1));
        blockSyncService.syncBlocks(1L, 1, 1, areaKey, now.plusSeconds(3));

        verifyBlocksSyncMessagesSent(2);
    }

    private void verifyBlocksSyncMessagesSent(int expectedCount) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(expectedCount)).convertAndSend(anyString(), captor.capture());
        for (Object payload : captor.getAllValues()) {
            assertThat(payload).isInstanceOf(WebSocketMessage.class);
            @SuppressWarnings("unchecked")
            WebSocketMessage<Object> message = (WebSocketMessage<Object>) payload;
            assertThat(message.getType()).isEqualTo(WebSocketEventType.BLOCKS_SYNC);
        }
    }
}
