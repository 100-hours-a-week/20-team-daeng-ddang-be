package com.daengddang.daengdong_map.domain.ranking;

import com.daengddang.daengdong_map.domain.dog.Dog;
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
        name = "region_dog_rank",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_region_dog_rank_period_region_dog",
                        columnNames = {"period_type", "period_value", "region_id", "dog_id"}
                )
        },
        indexes = {
                @Index(name = "idx_region_dog_rank_period_region_ranking", columnList = "period_type, period_value, region_id, ranking"),
                @Index(name = "idx_region_dog_rank_period_region_rate", columnList = "period_type, period_value, region_id, contribution_rate")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "region_dog_rank_seq_generator",
        sequenceName = "region_dog_rank_region_user_rank_id_seq",
        allocationSize = 1
)
public class RegionDogRank {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_dog_rank_seq_generator")
    @Column(name = "region_user_rank_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 10)
    private RankingPeriodType periodType;

    @Column(name = "period_value", nullable = false, length = 10)
    private String periodValue;

    @Column(name = "dog_distance", nullable = false)
    private Double dogDistance;

    @Column(name = "contribution_rate", nullable = false)
    private Double contributionRate;

    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Builder
    private RegionDogRank(RankingPeriodType periodType,
                          String periodValue,
                          Double dogDistance,
                          Double contributionRate,
                          Integer ranking,
                          Dog dog,
                          Region region) {
        this.periodType = periodType;
        this.periodValue = periodValue;
        this.dogDistance = dogDistance;
        this.contributionRate = contributionRate;
        this.ranking = ranking;
        this.dog = dog;
        this.region = region;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
