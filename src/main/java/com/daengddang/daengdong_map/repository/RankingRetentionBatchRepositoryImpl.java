package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class RankingRetentionBatchRepositoryImpl implements RankingRetentionBatchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RankingRetentionSummary deleteOlderThan(RankingPeriodType periodType, String cutoffPeriodValue) {
        Query query = entityManager.createNativeQuery("""
                with delete_dog_global as (
                    delete from dog_global_rank dgr
                    where dgr.period_type = :periodType
                      and dgr.period_value < :cutoffPeriodValue
                    returning 1
                ), delete_dog_region as (
                    delete from dog_rank dr
                    where dr.period_type = :periodType
                      and dr.period_value < :cutoffPeriodValue
                    returning 1
                ), delete_region as (
                    delete from region_rank rr
                    where rr.period_type = :periodType
                      and rr.period_value < :cutoffPeriodValue
                    returning 1
                ), delete_region_dog as (
                    delete from region_dog_rank rdr
                    where rdr.period_type = :periodType
                      and rdr.period_value < :cutoffPeriodValue
                    returning 1
                )
                select
                    (select count(*) from delete_dog_global),
                    (select count(*) from delete_dog_region),
                    (select count(*) from delete_region),
                    (select count(*) from delete_region_dog)
                """);

        query.setParameter("periodType", periodType.name());
        query.setParameter("cutoffPeriodValue", cutoffPeriodValue);

        Object[] result = (Object[]) query.getSingleResult();
        return new RankingRetentionSummary(
                ((Number) result[0]).intValue(),
                ((Number) result[1]).intValue(),
                ((Number) result[2]).intValue(),
                ((Number) result[3]).intValue()
        );
    }
}
