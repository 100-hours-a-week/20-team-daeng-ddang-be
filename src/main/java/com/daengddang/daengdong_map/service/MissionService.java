package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.dto.response.mission.MissionListResponse;
import com.daengddang.daengdong_map.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;

    @Transactional(readOnly = true)
    public MissionListResponse getMissions() {
        return MissionListResponse.from(missionRepository.findAll());
    }
}
