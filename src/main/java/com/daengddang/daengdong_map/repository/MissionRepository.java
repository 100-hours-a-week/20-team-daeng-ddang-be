package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.mission.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
}
