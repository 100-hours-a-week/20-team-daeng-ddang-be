package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository
public class RankingCleanupBatchRepositoryImpl implements RankingCleanupBatchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RankingCleanupSummary deleteObsoleteRanks(
            RankingPeriodType periodType,
            String periodValue,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        Query query = entityManager.createNativeQuery("""
                with base_walk as materialized (
                    select
                        w.dog_id
                    from walks w
                    where w.status = 'FINISHED'
                      and w.ended_at is not null
                      and w.distance is not null
                      and w.ended_at >= :startAt
                      and w.ended_at < :endAt
                ), valid_global_dog as (
                    select distinct bw.dog_id
                    from base_walk bw
                ), valid_dog_region as (
                    select distinct
                        d.dog_id,
                        u.region_id
                    from base_walk bw
                    join dogs d on d.dog_id = bw.dog_id
                    join users u on u.user_id = d.user_id
                    where u.region_id is not null
                      and d.status = 'ACTIVE'
                      and u.status = 'ACTIVE'
                ), valid_region as (
                    select distinct vdr.region_id
                    from valid_dog_region vdr
                ), delete_dog_global as (
                    delete from dog_global_rank dgr
                    where dgr.period_type = :periodType
                      and dgr.period_value = :periodValue
                      and not exists (
                        select 1
                        from valid_global_dog vgd
                        where vgd.dog_id = dgr.dog_id
                    )
                    returning 1
                ), delete_dog_region as (
                    delete from dog_rank dr
                    where dr.period_type = :periodType
                      and dr.period_value = :periodValue
                      and not exists (
                        select 1
                        from valid_dog_region vdr
                        where vdr.dog_id = dr.dog_id
                          and vdr.region_id = dr.region_id
                    )
                    returning 1
                ), delete_region as (
                    delete from region_rank rr
                    where rr.period_type = :periodType
                      and rr.period_value = :periodValue
                      and not exists (
                        select 1
                        from valid_region vr
                        where vr.region_id = rr.region_id
                    )
                    returning 1
                ), delete_region_dog as (
                    delete from region_dog_rank rdr
                    where rdr.period_type = :periodType
                      and rdr.period_value = :periodValue
                      and not exists (
                        select 1
                        from valid_dog_region vdr
                        where vdr.dog_id = rdr.dog_id
                          and vdr.region_id = rdr.region_id
                    )
                    returning 1
                )
                select
                    (select count(*) from delete_dog_global),
                    (select count(*) from delete_dog_region),
                    (select count(*) from delete_region),
                    (select count(*) from delete_region_dog)
                """);

        query.setParameter("periodType", periodType.name());
        query.setParameter("periodValue", periodValue);
        query.setParameter("startAt", startAt);
        query.setParameter("endAt", endAt);

        Object[] result = (Object[]) query.getSingleResult();
        return new RankingCleanupSummary(
                ((Number) result[0]).intValue(),
                ((Number) result[1]).intValue(),
                ((Number) result[2]).intValue(),
                ((Number) result[3]).intValue()
        );
    }
}
