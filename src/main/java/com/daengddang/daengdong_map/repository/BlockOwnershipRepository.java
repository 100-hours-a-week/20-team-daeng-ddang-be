package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.block.BlockOwnership;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.repository.projection.BlockOwnershipView;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockOwnershipRepository extends JpaRepository<BlockOwnership, Long> {

    List<BlockOwnership> findAllByDog(Dog dog);

    long countByDog(Dog dog);

    @Query("""
            select
                ownership.id as blockId,
                block.x as blockX,
                block.y as blockY,
                dog.id as dogId,
                ownership.acquiredAt as acquiredAt
            from BlockOwnership ownership
            join ownership.block block
            join ownership.dog dog
            where ownership.dog = :dog
            """)
    List<BlockOwnershipView> findAllByDogWithBlockAndDog(@Param("dog") Dog dog);

    List<BlockOwnership> findAllByIdIn(Collection<Long> blockIds);

    @Query("""
            SELECT
                ownership.id as blockId,
                block.x as blockX,
                block.y as blockY,
                dog.id as dogId,
                ownership.acquiredAt as acquiredAt
            FROM BlockOwnership ownership
            JOIN ownership.block block
            JOIN ownership.dog dog
            WHERE block.x BETWEEN :minX AND :maxX
              AND block.y BETWEEN :minY AND :maxY
            """)
    List<BlockOwnershipView> findAllByBlockRange(
            @Param("minX") int minX,
            @Param("maxX") int maxX,
            @Param("minY") int minY,
            @Param("maxY") int maxY
    );

    @Modifying
    @Query(value = """
            UPDATE block_ownership
            SET dog_id = :dogId,
                acquired_at = :updatedAt,
                last_passed_at = :updatedAt,
                updated_at = :updatedAt
            WHERE block_id = :blockId
            """, nativeQuery = true)
    void restoreOwner(
            @Param("blockId") Long blockId,
            @Param("dogId") Long dogId,
            @Param("updatedAt") java.time.LocalDateTime updatedAt
    );
}
