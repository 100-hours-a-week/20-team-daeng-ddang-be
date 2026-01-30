package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.domain.diary.WalkDiary;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.dto.request.diaries.WalkDiariesCreateRequest;
import com.daengddang.daengdong_map.dto.response.diaries.WalkDiariesCreateResponse;
import com.daengddang.daengdong_map.repository.WalkDiaryRepository;
import com.daengddang.daengdong_map.util.AccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WalkDiaryService {

    private final WalkDiaryRepository walkDiaryRepository;
    private final AccessValidator accessValidator;

    public WalkDiariesCreateResponse writeWalkDiary(WalkDiariesCreateRequest dto, Long userId, Long walkId) {

        User user = accessValidator.getUserOrThrow(userId);
        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);


        WalkDiary walkDiary = WalkDiariesCreateRequest.of(dto, user, walk);

        WalkDiary savedDiary = walkDiaryRepository.save(walkDiary);

        return WalkDiariesCreateResponse.from(savedDiary);
    }
}
