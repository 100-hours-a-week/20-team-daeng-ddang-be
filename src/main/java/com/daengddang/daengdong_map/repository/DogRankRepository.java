package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.DogRank;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.repository.projection.DogRankView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DogRankRepository extends JpaRepository<DogRank, Long> {

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.totalDistance as totalDistance
            from DogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
            order by rank.ranking asc
            """)
    List<DogRankView> findRanks(
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
                rank.totalDistance as totalDistance
            from DogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
            order by rank.totalDistance desc, dog.id asc
            """)
    List<DogRankView> findRanksAllRegions(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            Pageable pageable
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.totalDistance as totalDistance
            from DogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
              and dog.id = :dogId
            """)
    Optional<DogRankView> findMyRank(
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
                rank.totalDistance as totalDistance
            from DogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and dog.id = :dogId
            """)
    Optional<DogRankView> findMyRankAllRegions(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("dogId") Long dogId
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.totalDistance as totalDistance
            from DogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
              and rank.ranking = :ranking
            """)
    Optional<DogRankView> findByRanking(
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
                rank.totalDistance as totalDistance
            from DogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and rank.region.id = :regionId
              and (
                rank.totalDistance < :cursorDistance
                or (rank.totalDistance = :cursorDistance and dog.id > :cursorDogId)
              )
            order by rank.totalDistance desc, dog.id asc
            """)
    Slice<DogRankView> findRanksByCursor(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("regionId") Long regionId,
            @Param("cursorDistance") Double cursorDistance,
            @Param("cursorDogId") Long cursorDogId,
            Pageable pageable
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.profileImageUrl as profileImageUrl,
                rank.totalDistance as totalDistance
            from DogRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and (
                rank.totalDistance < :cursorDistance
                or (rank.totalDistance = :cursorDistance and dog.id > :cursorDogId)
              )
            order by rank.totalDistance desc, dog.id asc
            """)
    Slice<DogRankView> findRanksByCursorAllRegions(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("cursorDistance") Double cursorDistance,
            @Param("cursorDogId") Long cursorDogId,
            Pageable pageable
    );
}
