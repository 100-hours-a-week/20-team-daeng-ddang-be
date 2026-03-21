package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.DogGlobalRank;
import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import com.daengddang.daengdong_map.repository.projection.DogDistanceScoreView;
import com.daengddang.daengdong_map.repository.projection.DogRankView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DogGlobalRankRepository extends JpaRepository<DogGlobalRank, Long> {

    @Query("""
            select
                rank.dog.id as dogId,
                rank.totalDistance as totalDistance
            from DogGlobalRank rank
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
            """)
    List<DogDistanceScoreView> findScoresByPeriod(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.birthDate as birthDate,
                dog.profileImageUrl as profileImageUrl,
                dog.breed.name as dogBreed,
                rank.totalDistance as totalDistance
            from DogGlobalRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
            order by rank.ranking asc, dog.id asc
            """)
    List<DogRankView> findRanks(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            Pageable pageable
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.birthDate as birthDate,
                dog.profileImageUrl as profileImageUrl,
                dog.breed.name as dogBreed,
                rank.totalDistance as totalDistance
            from DogGlobalRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and dog.id = :dogId
            """)
    Optional<DogRankView> findMyRank(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("dogId") Long dogId
    );

    @Query("""
            select
                rank.ranking as rank,
                dog.id as dogId,
                dog.name as dogName,
                dog.birthDate as birthDate,
                dog.profileImageUrl as profileImageUrl,
                dog.breed.name as dogBreed,
                rank.totalDistance as totalDistance
            from DogGlobalRank rank
            join rank.dog dog
            where rank.periodType = :periodType
              and rank.periodValue = :periodValue
              and (
                rank.ranking > :cursorRank
                or (rank.ranking = :cursorRank and dog.id > :cursorDogId)
              )
            order by rank.ranking asc, dog.id asc
            """)
    Slice<DogRankView> findRanksByCursor(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodValue") String periodValue,
            @Param("cursorRank") Integer cursorRank,
            @Param("cursorDogId") Long cursorDogId,
            Pageable pageable
    );
}
