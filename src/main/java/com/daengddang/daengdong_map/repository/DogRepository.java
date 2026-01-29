package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DogRepository extends JpaRepository<Dog, Long> {

    Optional<Dog> findByUser(User user);

    @Query(value = "select * from dogs where user_id = :userId", nativeQuery = true)
    Optional<Dog> findByUserIdIncludingDeleted(@Param("userId") Long userId);

}
