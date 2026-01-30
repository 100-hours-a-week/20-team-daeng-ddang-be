package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoUserId(Long kakaoUserId);

    @Query(value = "select * from users where kakao_user_id = :kakaoUserId", nativeQuery = true)
    Optional<User> findByKakaoUserIdIncludingDeleted(@Param("kakaoUserId") Long kakaoUserId);

    @Query(value = "select * from users where user_id = :userId", nativeQuery = true)
    Optional<User> findByIdIncludingDeleted(@Param("userId") Long userId);

    boolean existsByKakaoUserId(Long kakaoUserId);

    @Query("""
            select user
            from User user
            left join fetch user.region region
            left join fetch region.parent parent
            where user.id = :userId
            """)
    Optional<User> findByIdWithRegion(@Param("userId") Long userId);
}
