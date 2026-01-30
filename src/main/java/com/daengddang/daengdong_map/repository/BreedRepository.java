package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.breed.Breed;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedRepository extends JpaRepository<Breed, Long> {
    List<Breed> findByNameContainingOrderByNameAsc(String keyword);
    Optional<Breed> findFirstByOrderByIdAsc();
}
