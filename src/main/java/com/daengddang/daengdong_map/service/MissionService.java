package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.dto.response.mission.MissionListResponse;
import com.daengddang.daengdong_map.repository.MissionRepository;
import com.daengddang.daengdong_map.service.cache.MissionCacheStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionCacheStore missionCacheStore;

    @Transactional(readOnly = true)
    public MissionListResponse getMissions() {
        return missionCacheStore.get().orElseGet(() -> {
            MissionListResponse response = MissionListResponse.from(missionRepository.findAll());
            missionCacheStore.put(response);
            return response;
        });
    }
}
