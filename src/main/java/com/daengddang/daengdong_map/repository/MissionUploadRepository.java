package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.mission.Mission;
import com.daengddang.daengdong_map.domain.mission.MissionUpload;
import com.daengddang.daengdong_map.domain.walk.Walk;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionUploadRepository extends JpaRepository<MissionUpload, Long> {

    Optional<MissionUpload> findByWalkAndMission(Walk walk, Mission mission);

    List<MissionUpload> findAllByWalk(Walk walk);
}
