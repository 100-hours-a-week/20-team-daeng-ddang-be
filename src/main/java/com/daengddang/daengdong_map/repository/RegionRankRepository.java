package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RegionRank;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.repository.projection.RegionRankView;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionRankRepository extends JpaRepository<RegionRank, Long> {

    @Query("""
            select
                rank.ranking as rank,
                region.id as regionId,
                region.name as regionName,
                rank.totalDistance as totalDistance
            from RegionRank rank
            join rank.region region
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
            order by rank.ranking asc
            """)
    Slice<RegionRankView> findRanks(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            Pageable pageable
    );

    @Query("""
            select
                rank.ranking as rank,
                region.id as regionId,
                region.name as regionName,
                rank.totalDistance as totalDistance
            from RegionRank rank
            join rank.region region
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and region.id = :regionId
            """)
    Optional<RegionRankView> findMyRegionRank(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("regionId") Long regionId
    );

    @Query("""
            select
                rank.ranking as rank,
                region.id as regionId,
                region.name as regionName,
                rank.totalDistance as totalDistance
            from RegionRank rank
            join rank.region region
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.ranking = :ranking
            """)
    Optional<RegionRankView> findByRanking(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("ranking") Integer ranking
    );

    @Query("""
            select
                rank.ranking as rank,
                region.id as regionId,
                region.name as regionName,
                rank.totalDistance as totalDistance
            from RegionRank rank
            join rank.region region
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and (
                rank.totalDistance < :cursorDistance
                or (rank.totalDistance = :cursorDistance and region.id > :cursorRegionId)
              )
            order by rank.totalDistance desc, region.id asc
            """)
    Slice<RegionRankView> findRanksByCursor(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("cursorDistance") Double cursorDistance,
            @Param("cursorRegionId") Long cursorRegionId,
            Pageable pageable
    );
}
