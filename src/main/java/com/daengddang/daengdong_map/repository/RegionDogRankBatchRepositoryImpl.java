package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class RegionDogRankBatchRepositoryImpl implements RegionDogRankBatchRepository {

    private static final String PERIOD_FILTER = """
            (
                (:periodType = 'YEAR' and to_char(w.ended_at, 'YYYY') = :periodValue)
                or (:periodType = 'MONTH' and to_char(w.ended_at, 'YYYY-MM') = :periodValue)
                or (:periodType = 'WEEK' and (to_char(w.ended_at, 'IYYY') || '-W' || to_char(w.ended_at, 'IW')) = :periodValue)
            )
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int upsertRanks(RankingPeriodType periodType, String periodValue) {
        Query query = entityManager.createNativeQuery("""
                with dog_region_aggregated as (
                    select
                        d.dog_id as dog_id,
                        u.region_id as region_id,
                        sum(w.distance) as dog_distance
                    from walks w
                    join dogs d on d.dog_id = w.dog_id
                    join users u on u.user_id = d.user_id
                    where w.status = 'FINISHED'
                      and w.ended_at is not null
                      and w.distance is not null
                      and u.region_id is not null
                      and d.status = 'ACTIVE'
                      and u.status = 'ACTIVE'
                      and """ + PERIOD_FILTER + """
                    group by d.dog_id, u.region_id
                ), region_total as (
                    select
                        dra.region_id,
                        sum(dra.dog_distance) as region_total_distance
                    from dog_region_aggregated dra
                    group by dra.region_id
                ), ranked as (
                    select
                        dra.dog_id,
                        dra.region_id,
                        dra.dog_distance,
                        case
                            when rt.region_total_distance > 0 then dra.dog_distance / rt.region_total_distance
                            else 0
                        end as contribution_rate,
                        row_number() over (
                            partition by dra.region_id
                            order by
                                (case
                                    when rt.region_total_distance > 0 then dra.dog_distance / rt.region_total_distance
                                    else 0
                                end) desc,
                                dra.dog_id asc
                        ) as ranking
                    from dog_region_aggregated dra
                    join region_total rt on rt.region_id = dra.region_id
                )
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
                    r.dog_distance,
                    r.contribution_rate,
                    r.ranking,
                    now(),
                    r.dog_id,
                    r.region_id
                from ranked r
                on conflict (period_type, period_value, region_id, dog_id)
                do update set
                    dog_distance = excluded.dog_distance,
                    contribution_rate = excluded.contribution_rate,
                    ranking = excluded.ranking
                """);
        bindCommonParams(query, periodType, periodValue);
        return query.executeUpdate();
    }

    @Override
    public int deleteObsoleteRanks(RankingPeriodType periodType, String periodValue) {
        Query query = entityManager.createNativeQuery("""
                delete from region_dog_rank rdr
                where rdr.period_type = :periodType
                  and rdr.period_value = :periodValue
                  and not exists (
                    select 1
                    from walks w
                    join dogs d on d.dog_id = w.dog_id
                    join users u on u.user_id = d.user_id
                    where d.dog_id = rdr.dog_id
                      and u.region_id = rdr.region_id
                      and w.status = 'FINISHED'
                      and w.ended_at is not null
                      and w.distance is not null
                      and u.region_id is not null
                      and d.status = 'ACTIVE'
                      and u.status = 'ACTIVE'
                      and """ + PERIOD_FILTER + """
                )
                """);
        bindCommonParams(query, periodType, periodValue);
        return query.executeUpdate();
    }

    private void bindCommonParams(Query query, RankingPeriodType periodType, String periodValue) {
        query.setParameter("periodType", periodType.name());
        query.setParameter("periodValue", periodValue);
    }
}
