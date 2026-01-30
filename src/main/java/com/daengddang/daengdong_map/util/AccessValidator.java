package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AccessValidator {

    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final WalkRepository walkRepository;

    public User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));
    }

    public Dog getDogOrThrow(Long userId) {
        User user = getUserOrThrow(userId);
        return dogRepository.findByUser(user)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public Walk getOwnedWalkOrThrow(Long userId, Long walkId) {
        if (userId == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new BaseException(ErrorCode.WALK_RECORD_NOT_FOUND));
        if (!userId.equals(walk.getDog().getUser().getId())) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        return walk;
    }
}
