package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalkRepository extends JpaRepository<Walk, Long> {

    Optional<Walk> findFirstByDogAndStatus(Dog dog, WalkStatus status);

    Optional<Walk> findByIdAndDog(Long id, Dog dog);

    boolean existsByDogAndStatus(Dog dog, WalkStatus status);

    @Query("""
            select count(walk) as totalCount,
                   coalesce(sum(walk.distance), 0.0)
                   as totalDistance
            from Walk walk
            where walk.dog = :dog
              and walk.status = :status
            """)
    WalkSummary findSummaryByDogAndStatus(
            @Param("dog") Dog dog,
            @Param("status") WalkStatus status
    );
}
