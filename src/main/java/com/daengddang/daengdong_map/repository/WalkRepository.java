package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.domain.walk.WalkStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkRepository extends JpaRepository<Walk, Long> {

    Optional<Walk> findFirstByDogAndStatus(Dog dog, WalkStatus status);

    Optional<Walk> findByIdAndDog(Long id, Dog dog);

    boolean existsByDogAndStatus(Dog dog, WalkStatus status);
}
