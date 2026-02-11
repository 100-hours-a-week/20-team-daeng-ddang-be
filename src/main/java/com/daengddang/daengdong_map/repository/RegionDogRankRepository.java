package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.domain.ranking.RegionDogRank;
import com.daengddang.daengdong_map.repository.projection.RegionContributionRankView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionDogRankRepository extends JpaRepository<RegionDogRank, Long> {

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.dogDistance as dogDistance,
                rank.contributionRate as contributionRate
            from RegionDogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
            order by rank.ranking asc
            """)
    List<RegionContributionRankView> findRanks(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("regionId") Long regionId,
            Pageable pageable
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.dogDistance as dogDistance,
                rank.contributionRate as contributionRate
            from RegionDogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
              and dog.id = :dogId
            """)
    Optional<RegionContributionRankView> findMyRank(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("regionId") Long regionId,
            @Param("dogId") Long dogId
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.dogDistance as dogDistance,
                rank.contributionRate as contributionRate
            from RegionDogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
              and rank.ranking = :ranking
            """)
    Optional<RegionContributionRankView> findByRanking(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("regionId") Long regionId,
            @Param("ranking") Integer ranking
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.dogDistance as dogDistance,
                rank.contributionRate as contributionRate
            from RegionDogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
              and (
                rank.contributionRate < :cursorRate
                or (rank.contributionRate = :cursorRate and dog.id > :cursorDogId)
              )
            order by rank.contributionRate desc, dog.id asc
            """)
    Slice<RegionContributionRankView> findRanksByCursor(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("regionId") Long regionId,
            @Param("cursorRate") Double cursorRate,
            @Param("cursorDogId") Long cursorDogId,
            Pageable pageable
    );
}
