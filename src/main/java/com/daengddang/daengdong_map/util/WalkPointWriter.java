package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.domain.walk.Walk;
import java.time.LocalDateTime;

public interface WalkPointWriter {

    void save(Walk walk, double latitude, double longitude, LocalDateTime recordedAt);

}
