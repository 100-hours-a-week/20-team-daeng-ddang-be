package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkPoint;
import com.daengddang.daengdong_map.repository.WalkPointRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaWalkPointWriter implements WalkPointWriter {

    private final WalkPointRepository walkPointRepository;

    @Override
    public void save(Walk walk, double latitude, double longitude, LocalDateTime recordedAt) {
        WalkPoint point = WalkPoint.builder()
                .latitude(latitude)
                .longitude(longitude)
                .recordedAt(recordedAt)
                .walk(walk)
                .build();
        walkPointRepository.save(point);
    }
}
