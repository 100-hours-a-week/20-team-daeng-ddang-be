package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.mission.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {
}
