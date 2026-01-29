package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.expression.Expression;
import com.daengddang.daengdong_map.domain.walk.Walk;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpressionRepository extends JpaRepository<Expression, Long> {

    boolean existsByWalk(Walk walk);

    Optional<Expression> findByWalk(Walk walk);
}
