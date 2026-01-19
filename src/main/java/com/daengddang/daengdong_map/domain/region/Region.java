package com.daengddang.daengdong_map.domain.region;

import com.daengddang.daengdong_map.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "regions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "region_seq_generator",
        sequenceName = "regions_region_id_seq",
        allocationSize = 1
)
public class Region extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_seq_generator")
    @Column(name = "region_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 10)
    private RegionLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Region parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private RegionStatus status = RegionStatus.ACTIVE;

    @Builder
    private Region(String name, RegionLevel level, Region parent, RegionStatus status) {
        this.name = name;
        this.level = level;
        this.parent = parent;
        this.status = status;
    }

    public void updateStatus(RegionStatus status) {
        this.status = status;
    }

    public void changeParent(Region parent) {
        this.parent = parent;
    }
}
