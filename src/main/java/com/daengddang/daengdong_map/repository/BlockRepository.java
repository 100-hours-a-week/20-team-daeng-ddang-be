package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.block.Block;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByXAndY(Integer x, Integer y);

    List<Block> findByXBetweenAndYBetween(Integer minX, Integer maxX, Integer minY, Integer maxY);

    @Modifying
    @Query(value = """
            INSERT INTO blocks (x, y, created_at)
            VALUES (:x, :y, NOW())
            ON CONFLICT (x, y) DO NOTHING
            """, nativeQuery = true)
    void insertIfNotExists(@Param("x") Integer x, @Param("y") Integer y);
}
