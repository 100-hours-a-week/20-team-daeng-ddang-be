package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.dto.response.mission.MissionListResponse;
import com.daengddang.daengdong_map.repository.MissionRepository;
import com.daengddang.daengdong_map.service.cache.MissionCacheMetrics;
import com.daengddang.daengdong_map.service.cache.MissionCacheStore;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionCacheStore missionCacheStore;
    private final MissionCacheMetrics missionCacheMetrics;
    private final AtomicReference<CompletableFuture<MissionListResponse>> inFlightLoad = new AtomicReference<>();

    @Transactional(readOnly = true)
    public MissionListResponse getMissions() {
        Optional<MissionListResponse> cached = missionCacheStore.get();
        if (cached.isPresent()) {
            return cached.get();
        }

        while (true) {
            CompletableFuture<MissionListResponse> existing = inFlightLoad.get();
            if (existing != null) {
                return existing.join();
            }

            CompletableFuture<MissionListResponse> created = new CompletableFuture<>();
            if (inFlightLoad.compareAndSet(null, created)) {
                try {
                    MissionListResponse response = loadFromDbAndCache();
                    created.complete(response);
                    return response;
                } catch (Exception e) {
                    created.completeExceptionally(e);
                    throw e;
                } finally {
                    inFlightLoad.compareAndSet(created, null);
                }
            }
        }
    }

    private MissionListResponse loadFromDbAndCache() {
        missionCacheMetrics.recordDbLoad();
        MissionListResponse response = MissionListResponse.from(missionRepository.findAll());
        missionCacheStore.put(response);
        return response;
    }
}
