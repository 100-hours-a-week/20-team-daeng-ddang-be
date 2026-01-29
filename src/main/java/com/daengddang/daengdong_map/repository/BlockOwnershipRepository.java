package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.dog.Dog;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockOwnershipRepository extends JpaRepository<BlockOwnership, Long> {

    List<BlockOwnership> findAllByDog(Dog dog);

    List<BlockOwnership> findAllByIdIn(Collection<Long> blockIds);

    @Query("""
            select ownership
            from BlockOwnership ownership
            join fetch ownership.block block
            where block.x between :minX and :maxX
              and block.y between :minY and :maxY
            """)
    List<BlockOwnership> findAllByBlockRange(
            @Param("minX") int minX,
            @Param("maxX") int maxX,
            @Param("minY") int minY,
            @Param("maxY") int maxY
    );
}
