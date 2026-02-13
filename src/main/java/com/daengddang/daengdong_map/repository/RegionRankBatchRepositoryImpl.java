package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.ranking.RankingPeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class RegionRankBatchRepositoryImpl implements RegionRankBatchRepository {

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
                with aggregated as (
                    select
                        u.region_id as region_id,
                        sum(w.distance) as total_distance
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
                    group by u.region_id
                ), ranked as (
                    select
                        a.region_id,
                        a.total_distance,
                        row_number() over (order by a.total_distance desc, a.region_id asc) as ranking
                    from aggregated a
                )
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
                    r.total_distance,
                    r.ranking,
                    now(),
                    r.region_id
                from ranked r
                on conflict (period_type, period_value, region_id)
                do update set
                    total_distance = excluded.total_distance,
                    ranking = excluded.ranking
                """);
        bindCommonParams(query, periodType, periodValue);
        return query.executeUpdate();
    }

    @Override
    public int deleteObsoleteRanks(RankingPeriodType periodType, String periodValue) {
        Query query = entityManager.createNativeQuery("""
                delete from region_rank rr
                where rr.period_type = :periodType
                  and rr.period_value = :periodValue
                  and not exists (
                    select 1
                    from walks w
                    join dogs d on d.dog_id = w.dog_id
                    join users u on u.user_id = d.user_id
                    where u.region_id = rr.region_id
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
