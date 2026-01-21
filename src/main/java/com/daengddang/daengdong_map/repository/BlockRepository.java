package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.block.Block;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByXAndY(Integer x, Integer y);

    List<Block> findByXBetweenAndYBetween(Integer minX, Integer maxX, Integer minY, Integer maxY);
}
