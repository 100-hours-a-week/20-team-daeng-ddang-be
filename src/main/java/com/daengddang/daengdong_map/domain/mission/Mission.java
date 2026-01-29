package com.daengddang.daengdong_map.domain.mission;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "missions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "mission_seq_generator",
        sequenceName = "missions_mission_id_seq",
        allocationSize = 1
)
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mission_seq_generator")
    @Column(name = "mission_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 80)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 10)
    private MissionDifficulty difficulty;

    @Column(name = "mission_type", nullable = false, length = 30)
    private String missionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Mission(String title, String description, MissionDifficulty difficulty, String missionType) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.missionType = missionType;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
