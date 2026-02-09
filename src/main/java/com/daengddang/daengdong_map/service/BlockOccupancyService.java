package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.domain.block.Block;
import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.repository.BlockOwnershipRepository;
import com.daengddang.daengdong_map.repository.BlockRepository;
import com.daengddang.daengdong_map.repository.WalkBlockLogRepository;
import java.time.LocalDateTime;

import com.daengddang.daengdong_map.util.BlockOccupancyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockOccupancyService {

    private final BlockRepository blockRepository;
    private final BlockOwnershipRepository blockOwnershipRepository;
    private final WalkBlockLogRepository walkBlockLogRepository;

    @Transactional
    public BlockOccupancyResult occupy(Walk walk, int blockX, int blockY, LocalDateTime timestamp) {
        Dog dog = walk.getDog();

        Long blockId = blockRepository.insertIfNotExistsReturningId(blockX, blockY);
        BlockOwnership ownership = blockOwnershipRepository.findById(blockId).orElse(null);
        if (ownership == null) {
            Block block = blockRepository.getReferenceById(blockId);
            BlockOwnership newOwnership = BlockOwnership.builder()
                    .block(block)
                    .dog(dog)
                    .acquiredAt(timestamp)
                    .lastPassedAt(timestamp)
                    .build();
            blockOwnershipRepository.save(newOwnership);
            walkBlockLogRepository.insertIfNotExists(walk.getId(), blockId, dog.getId(), null, timestamp);
            return BlockOccupancyResult.occupied();
        }

        if (ownership.getDog().getId().equals(dog.getId())) {
            ownership.updateLastPassedAt(timestamp);
            return BlockOccupancyResult.alreadyOwned();
        }

        Long previousDogId = ownership.getDog().getId();
        ownership.updateOwner(dog, timestamp);
        walkBlockLogRepository.insertIfNotExists(walk.getId(), blockId, dog.getId(), previousDogId, timestamp);
        return BlockOccupancyResult.taken(previousDogId);
    }
}
