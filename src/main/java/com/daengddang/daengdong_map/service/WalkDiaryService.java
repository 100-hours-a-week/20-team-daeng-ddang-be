package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.diary.WalkDiary;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.dto.request.diaries.WalkDiariesCreateRequest;
import com.daengddang.daengdong_map.dto.response.diaries.WalkDiariesCreateResponse;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.repository.WalkDiaryRepository;
import com.daengddang.daengdong_map.repository.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WalkDiaryService {

    private final WalkDiaryRepository walkDiaryRepository;
    private final UserRepository userRepository;
    private final WalkRepository walkRepository;

    public WalkDiariesCreateResponse writeWalkDiary(WalkDiariesCreateRequest dto, Long userId, Long walkId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BaseException(ErrorCode.UNAUTHORIZED)
        );

        Walk walk = walkRepository.findById(walkId).orElseThrow(
                () -> new BaseException(ErrorCode.WALK_RECORD_NOT_FOUND)
        );


        WalkDiary walkDiary = WalkDiariesCreateRequest.of(dto, user, walk);

        WalkDiary savedDiary = walkDiaryRepository.save(walkDiary);

        return WalkDiariesCreateResponse.from(savedDiary);
    }
}
