package com.daengddang.daengdong_map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daengddang.daengdong_map.common.BlockIdUtil;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.block.Block;
import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockListResponse;
import com.daengddang.daengdong_map.dto.response.block.NearbyBlockResponse;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @Mock
    private BlockOwnershipRepository blockOwnershipRepository;

    @InjectMocks
    private BlockService blockService;

    @Test
    void getNearbyBlocks_throwsWhenInputInvalid() {
        assertThatThrownBy(() -> blockService.getNearbyBlocks(null, 1.0, 10))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FORMAT);

        assertThatThrownBy(() -> blockService.getNearbyBlocks(Double.NaN, 1.0, 10))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FORMAT);

        assertThatThrownBy(() -> blockService.getNearbyBlocks(1.0, 1.0, 0))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FORMAT);
    }

    @Test
    void getNearbyBlocks_fetchesRangeAndMapsResponse() {
        double lat = BlockIdUtil.BLOCK_SIZE * 2.0;
        double lng = BlockIdUtil.BLOCK_SIZE * 2.0;
        int radiusMeters = 160;

        User user = User.builder().kakaoUserId(1L).build();
        Breed breed = Breed.builder().name("mix").build();
        Dog dog = Dog.builder().name("coco").user(user).breed(breed).build();
        ReflectionTestUtils.setField(dog, "id", 99L);

        Block block = Block.builder().x(2).y(3).build();
        BlockOwnership ownership = BlockOwnership.builder()
                .block(block)
                .dog(dog)
                .acquiredAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .lastPassedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        when(blockOwnershipRepository.findAllByBlockRange(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(ownership));

        NearbyBlockListResponse response = blockService.getNearbyBlocks(lat, lng, radiusMeters);

        ArgumentCaptor<Integer> minX = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxX = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> minY = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxY = ArgumentCaptor.forClass(Integer.class);

        verify(blockOwnershipRepository).findAllByBlockRange(minX.capture(), maxX.capture(), minY.capture(), maxY.capture());

        assertThat(minX.getValue()).isEqualTo(0);
        assertThat(maxX.getValue()).isEqualTo(4);
        assertThat(minY.getValue()).isEqualTo(0);
        assertThat(maxY.getValue()).isEqualTo(4);

        assertThat(response.getBlocks()).hasSize(1);
        NearbyBlockResponse blockResponse = response.getBlocks().get(0);
        assertThat(blockResponse.getBlockId()).isEqualTo(BlockIdUtil.toBlockId(2, 3));
        assertThat(blockResponse.getDogId()).isEqualTo(99L);
        assertThat(blockResponse.getOccupiedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
    }
}
