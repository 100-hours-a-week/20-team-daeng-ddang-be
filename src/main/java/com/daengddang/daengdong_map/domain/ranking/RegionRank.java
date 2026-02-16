package com.daengddang.daengdong_map.domain.ranking;

import com.daengddang.daengdong_map.domain.region.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "region_rank",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_region_rank_period_region",
                        columnNames = {"period_type", "period_value", "region_id"}
                )
        },
        indexes = {
                @Index(name = "idx_region_rank_period_ranking", columnList = "period_type, period_value, ranking"),
                @Index(name = "idx_region_rank_period_distance", columnList = "period_type, period_value, total_distance")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "region_rank_seq_generator",
        sequenceName = "region_rank_region_rank_id_seq",
        allocationSize = 1
)
public class RegionRank {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_rank_seq_generator")
    @Column(name = "region_rank_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 10)
    private RankingPeriodType periodType;

    @Column(name = "period_value", nullable = false, length = 10)
    private String periodValue;

    @Column(name = "total_distance", nullable = false)
    private Double totalDistance;

    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Builder
    private RegionRank(RankingPeriodType periodType,
                       String periodValue,
                       Double totalDistance,
                       Integer ranking,
                       Region region) {
        this.periodType = periodType;
        this.periodValue = periodValue;
        this.totalDistance = totalDistance;
        this.ranking = ranking;
        this.region = region;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
