package com.daengddang.daengdong_map.domain.walk;

import com.daengddang.daengdong_map.domain.dog.Dog;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "walks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "walk_seq_generator",
        sequenceName = "walks_walk_id_seq",
        allocationSize = 1
)
public class Walk {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "walk_seq_generator")
    @Column(name = "walk_id")
    private Long id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "duration")
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WalkStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @Builder
    private Walk(LocalDateTime startedAt,
                LocalDateTime endedAt,
                Double distance,
                Integer duration,
                WalkStatus status,
                Dog dog) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.distance = distance;
        this.duration = duration;
        this.status = status == null ? WalkStatus.IN_PROGRESS : status;
        this.dog = dog;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = WalkStatus.IN_PROGRESS;
        }
    }

    public void finish(LocalDateTime endedAt, Double distance, Integer duration) {
        this.endedAt = endedAt;
        this.distance = distance;
        this.duration = duration;
        this.status = WalkStatus.FINISHED;
    }
}
