package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.analysis.Analysis;
import com.daengddang.daengdong_map.repository.projection.DailyRecordView;
import com.daengddang.daengdong_map.repository.projection.DateCountView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    @Query(value = """
            SELECT CAST(a.created_at AS DATE) AS date, COUNT(*) AS count
            FROM analysis a
            JOIN dogs d ON d.dog_id = a.dog_id
            WHERE d.user_id = :userId
              AND a.created_at >= :startAt
              AND a.created_at < :endAt
            GROUP BY CAST(a.created_at AS DATE)
            """, nativeQuery = true)
    List<DateCountView> countHealthCareByDate(
            @Param("userId") Long userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query(value = """
            SELECT a.analysis_id AS id, a.created_at AS createdAt
            FROM analysis a
            JOIN dogs d ON d.dog_id = a.dog_id
            WHERE d.user_id = :userId
              AND a.created_at >= :startAt
              AND a.created_at < :endAt
            ORDER BY a.created_at ASC
            """, nativeQuery = true)
    List<DailyRecordView> findDailyHealthRecords(
            @Param("userId") Long userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

}
