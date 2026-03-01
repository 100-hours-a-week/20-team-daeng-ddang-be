package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.mission.MissionRecord;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {

    @EntityGraph(attributePaths = {"mission"})
    List<MissionRecord> findAllByWalk_Id(Long walkId);
}
