package com.daengddang.daengdong_map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkPoint;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import com.daengddang.daengdong_map.dto.request.walk.WalkEndRequest;
import com.daengddang.daengdong_map.dto.request.walk.WalkStartRequest;
import com.daengddang.daengdong_map.dto.response.walk.OccupiedBlockListResponse;
import com.daengddang.daengdong_map.dto.response.walk.OccupiedBlockResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkEndResponse;
import com.daengddang.daengdong_map.dto.response.walk.WalkStartResponse;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.repository.WalkPointRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WalkServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DogRepository dogRepository;

    @Mock
    private WalkRepository walkRepository;

    @Mock
    private WalkPointRepository walkPointRepository;

    @Mock
    private BlockOwnershipRepository blockOwnershipRepository;

    @InjectMocks
    private WalkService walkService;

    @Test
    void startWalk_throwsWhenRequestIsNull() {
        assertThatThrownBy(() -> walkService.startWalk(1L, null))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FORMAT);
    }

    @Test
    void startWalk_throwsWhenWalkAlreadyInProgress() {
        User user = user(1L);
        Dog dog = dog(user, 10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dogRepository.findByUser(user)).thenReturn(Optional.of(dog));
        when(walkRepository.existsByDogAndStatus(dog, WalkStatus.IN_PROGRESS)).thenReturn(true);

        WalkStartRequest request = startRequest(37.0, 127.0);

        assertThatThrownBy(() -> walkService.startWalk(1L, request))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FORMAT);
    }

    @Test
    void startWalk_savesWalkAndStartPoint() {
        User user = user(1L);
        Dog dog = dog(user, 10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dogRepository.findByUser(user)).thenReturn(Optional.of(dog));
        when(walkRepository.existsByDogAndStatus(dog, WalkStatus.IN_PROGRESS)).thenReturn(false);
        when(walkRepository.save(any(Walk.class))).thenAnswer(invocation -> {
            Walk saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 77L);
            return saved;
        });

        WalkStartRequest request = startRequest(37.5, 127.1);

        WalkStartResponse response = walkService.startWalk(1L, request);

        ArgumentCaptor<WalkPoint> walkPointCaptor = ArgumentCaptor.forClass(WalkPoint.class);
        verify(walkPointRepository).save(walkPointCaptor.capture());

        WalkPoint savedPoint = walkPointCaptor.getValue();
        assertThat(savedPoint.getLatitude()).isEqualTo(37.5);
        assertThat(savedPoint.getLongitude()).isEqualTo(127.1);

        assertThat(response.getWalkId()).isEqualTo(77L);
        assertThat(response.getStatus()).isEqualTo(WalkStatus.IN_PROGRESS);
        assertThat(response.getStartedAt()).isNotNull();
    }

    @Test
    void endWalk_validatesStatusAndMetrics() {
        User user = user(1L);
        Dog dog = dog(user, 10L);
        Walk walk = walk(dog, 100L, WalkStatus.IN_PROGRESS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dogRepository.findByUser(user)).thenReturn(Optional.of(dog));
        when(walkRepository.findByIdAndDog(100L, dog)).thenReturn(Optional.of(walk));

        WalkEndRequest wrongStatus = endRequest(37.0, 127.0, 1.0, 600, WalkStatus.IN_PROGRESS);
        assertThatThrownBy(() -> walkService.endWalk(1L, 100L, wrongStatus))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FORMAT);

        WalkEndRequest invalidMetrics = endRequest(37.0, 127.0, -1.0, 0, WalkStatus.FINISHED);
        assertThatThrownBy(() -> walkService.endWalk(1L, 100L, invalidMetrics))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_WALK_METRICS);
    }

    @Test
    void endWalk_updatesWalkAndReturnsSummary() {
        User user = user(1L);
        Dog dog = dog(user, 10L);
        Walk walk = walk(dog, 100L, WalkStatus.IN_PROGRESS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dogRepository.findByUser(user)).thenReturn(Optional.of(dog));
        when(walkRepository.findByIdAndDog(100L, dog)).thenReturn(Optional.of(walk));
        when(blockOwnershipRepository.findAllByDog(dog))
                .thenReturn(List.of(org.mockito.Mockito.mock(BlockOwnership.class),
                        org.mockito.Mockito.mock(BlockOwnership.class)));

        WalkEndRequest request = endRequest(37.0, 127.0, 1.5, 600, WalkStatus.FINISHED);

        WalkEndResponse response = walkService.endWalk(1L, 100L, request);

        ArgumentCaptor<WalkPoint> walkPointCaptor = ArgumentCaptor.forClass(WalkPoint.class);
        verify(walkPointRepository).save(walkPointCaptor.capture());
        WalkPoint savedPoint = walkPointCaptor.getValue();
        assertThat(savedPoint.getLatitude()).isEqualTo(37.0);
        assertThat(savedPoint.getLongitude()).isEqualTo(127.0);

        assertThat(walk.getStatus()).isEqualTo(WalkStatus.FINISHED);
        assertThat(walk.getDistance()).isEqualTo(1500.0);
        assertThat(walk.getDuration()).isEqualTo(600);
        assertThat(walk.getEndedAt()).isNotNull();

        assertThat(response.getWalkId()).isEqualTo(100L);
        assertThat(response.getTotalDistanceKm()).isEqualTo(1.5);
        assertThat(response.getDurationSeconds()).isEqualTo(600);
        assertThat(response.getOccupiedBlockCount()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(WalkStatus.FINISHED);
    }

    @Test
    void getOccupiedBlocks_mapsBlockOwnerships() {
        User user = user(1L);
        Dog dog = dog(user, 10L);
        Walk walk = walk(dog, 100L, WalkStatus.IN_PROGRESS);

        Block block = Block.builder().x(3).y(4).build();
        BlockOwnership ownership = BlockOwnership.builder()
                .block(block)
                .dog(dog)
                .acquiredAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .lastPassedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(dogRepository.findByUser(user)).thenReturn(Optional.of(dog));
        when(walkRepository.findByIdAndDog(100L, dog)).thenReturn(Optional.of(walk));
        when(blockOwnershipRepository.findAllByDog(dog)).thenReturn(List.of(ownership));

        OccupiedBlockListResponse response = walkService.getOccupiedBlocks(1L, 100L);

        assertThat(response.getBlocks()).hasSize(1);
        OccupiedBlockResponse blockResponse = response.getBlocks().get(0);
        assertThat(blockResponse.getBlockId()).isEqualTo(BlockIdUtil.toBlockId(3, 4));
        assertThat(blockResponse.getDogId()).isEqualTo(10L);
        assertThat(blockResponse.getOccupiedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
    }

    private User user(Long id) {
        User user = User.builder().kakaoUserId(111L).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Dog dog(User user, Long id) {
        Breed breed = Breed.builder().name("mix").build();
        Dog dog = Dog.builder().name("coco").user(user).breed(breed).build();
        ReflectionTestUtils.setField(dog, "id", id);
        return dog;
    }

    private Walk walk(Dog dog, Long id, WalkStatus status) {
        Walk walk = Walk.builder()
                .dog(dog)
                .startedAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .status(status)
                .build();
        ReflectionTestUtils.setField(walk, "id", id);
        return walk;
    }

    private WalkStartRequest startRequest(double lat, double lng) {
        WalkStartRequest request = new WalkStartRequest();
        ReflectionTestUtils.setField(request, "startLat", lat);
        ReflectionTestUtils.setField(request, "startLng", lng);
        return request;
    }

    private WalkEndRequest endRequest(double lat, double lng, double distanceKm, int durationSeconds, WalkStatus status) {
        WalkEndRequest request = new WalkEndRequest();
        ReflectionTestUtils.setField(request, "endLat", lat);
        ReflectionTestUtils.setField(request, "endLng", lng);
        ReflectionTestUtils.setField(request, "totalDistanceKm", distanceKm);
        ReflectionTestUtils.setField(request, "durationSeconds", durationSeconds);
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }
}
