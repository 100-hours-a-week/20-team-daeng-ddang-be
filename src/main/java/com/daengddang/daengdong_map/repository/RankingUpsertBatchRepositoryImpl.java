package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository
public class RankingUpsertBatchRepositoryImpl implements RankingUpsertBatchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RankingUpsertSummary upsertAllRanks(
            RankingPeriodType periodType,
            String periodValue,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        Query query = entityManager.createNativeQuery("""
                with base_walk as materialized (
                    select
                        w.dog_id,
                        w.distance
                    from walks w
                    where w.status = 'FINISHED'
                      and w.ended_at is not null
                      and w.distance is not null
                      and w.ended_at >= :startAt
                      and w.ended_at < :endAt
                ), global_aggregated as (
                    select
                        bw.dog_id,
                        sum(bw.distance) as total_distance
                    from base_walk bw
                    group by bw.dog_id
                ), global_ranked as (
                    select
                        ga.dog_id,
                        ga.total_distance,
                        row_number() over (
                            order by ga.total_distance desc, ga.dog_id asc
                        ) as ranking
                    from global_aggregated ga
                ), upsert_global as (
                    insert into dog_global_rank (
                        period_type,
                        period_value,
                        total_distance,
                        ranking,
                        created_at,
                        dog_id
                    )
                    select
                        :periodType,
                        :periodValue,
                        gr.total_distance,
                        gr.ranking,
                        now(),
                        gr.dog_id
                    from global_ranked gr
                    on conflict (period_type, period_value, dog_id)
                    do update set
                        total_distance = excluded.total_distance,
                        ranking = excluded.ranking
                    returning 1
                ), dog_region_aggregated as (
                    select
                        d.dog_id,
                        u.region_id,
                        sum(bw.distance) as total_distance
                    from base_walk bw
                    join dogs d on d.dog_id = bw.dog_id
                    join users u on u.user_id = d.user_id
                    where u.region_id is not null
                      and d.status = 'ACTIVE'
                      and u.status = 'ACTIVE'
                    group by d.dog_id, u.region_id
                ), dog_region_ranked as (
                    select
                        dra.dog_id,
                        dra.region_id,
                        dra.total_distance,
                        row_number() over (
                            partition by dra.region_id
                            order by dra.total_distance desc, dra.dog_id asc
                        ) as ranking
                    from dog_region_aggregated dra
                ), upsert_dog_region as (
                    insert into dog_rank (
                        period_type,
                        period_value,
                        total_distance,
                        ranking,
                        created_at,
                        dog_id,
                        region_id
                    )
                    select
                        :periodType,
                        :periodValue,
                        drr.total_distance,
                        drr.ranking,
                        now(),
                        drr.dog_id,
                        drr.region_id
                    from dog_region_ranked drr
                    on conflict (period_type, period_value, region_id, dog_id)
                    do update set
                        total_distance = excluded.total_distance,
                        ranking = excluded.ranking
                    returning 1
                ), region_aggregated as (
                    select
                        dra.region_id,
                        sum(dra.total_distance) as total_distance
                    from dog_region_aggregated dra
                    group by dra.region_id
                ), region_ranked as (
                    select
                        ra.region_id,
                        ra.total_distance,
                        row_number() over (
                            order by ra.total_distance desc, ra.region_id asc
                        ) as ranking
                    from region_aggregated ra
                ), upsert_region as (
                    insert into region_rank (
                        period_type,
                        period_value,
                        total_distance,
                        ranking,
                        created_at,
                        region_id
                    )
                    select
                        :periodType,
                        :periodValue,
                        rr.total_distance,
                        rr.ranking,
                        now(),
                        rr.region_id
                    from region_ranked rr
                    on conflict (period_type, period_value, region_id)
                    do update set
                        total_distance = excluded.total_distance,
                        ranking = excluded.ranking
                    returning 1
                ), region_dog_ranked as (
                    select
                        dra.dog_id,
                        dra.region_id,
                        dra.total_distance as dog_distance,
                        case
                            when ra.total_distance > 0 then dra.total_distance / ra.total_distance
                            else 0
                        end as contribution_rate,
                        row_number() over (
                            partition by dra.region_id
                            order by
                                (case
                                    when ra.total_distance > 0 then dra.total_distance / ra.total_distance
                                    else 0
                                end) desc,
                                dra.dog_id asc
                        ) as ranking
                    from dog_region_aggregated dra
                    join region_aggregated ra on ra.region_id = dra.region_id
                ), upsert_region_dog as (
                    insert into region_dog_rank (
                        period_type,
                        period_value,
                        dog_distance,
                        contribution_rate,
                        ranking,
                        created_at,
                        dog_id,
                        region_id
                    )
                    select
                        :periodType,
                        :periodValue,
                        rdr.dog_distance,
                        rdr.contribution_rate,
                        rdr.ranking,
                        now(),
                        rdr.dog_id,
                        rdr.region_id
                    from region_dog_ranked rdr
                    on conflict (period_type, period_value, region_id, dog_id)
                    do update set
                        dog_distance = excluded.dog_distance,
                        contribution_rate = excluded.contribution_rate,
                        ranking = excluded.ranking
                    returning 1
                )
                select
                    (select count(*) from upsert_global),
                    (select count(*) from upsert_dog_region),
                    (select count(*) from upsert_region),
                    (select count(*) from upsert_region_dog)
                """);

        query.setParameter("periodType", periodType.name());
        query.setParameter("periodValue", periodValue);
        query.setParameter("startAt", startAt);
        query.setParameter("endAt", endAt);

        Object[] result = (Object[]) query.getSingleResult();
        return new RankingUpsertSummary(
                ((Number) result[0]).intValue(),
                ((Number) result[1]).intValue(),
                ((Number) result[2]).intValue(),
                ((Number) result[3]).intValue()
        );
    }
}
