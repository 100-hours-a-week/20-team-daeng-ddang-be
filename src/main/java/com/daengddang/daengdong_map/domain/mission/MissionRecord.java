package com.daengddang.daengdong_map.domain.mission;

import com.daengddang.daengdong_map.domain.walk.Walk;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mission_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "mission_record_seq_generator",
        sequenceName = "mission_records_mission_record_id_seq",
        allocationSize = 1
)
public class MissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mission_record_seq_generator")
    @Column(name = "mission_record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id", nullable = false)
    private Walk walk;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private MissionRecordStatus status;

    @Column(name = "message", length = 300)
    private String message;

    @Column(name = "mission_video_url", nullable = false, length = 1000)
    private String missionVideoUrl;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @Builder
    private MissionRecord(Mission mission,
                          Walk walk,
                          MissionRecordStatus status,
                          String message,
                          String missionVideoUrl,
                          LocalDateTime submittedAt,
                          LocalDateTime analyzedAt) {
        this.mission = mission;
        this.walk = walk;
        this.status = status;
        this.message = message;
        this.missionVideoUrl = missionVideoUrl;
        this.submittedAt = submittedAt;
        this.analyzedAt = analyzedAt;
    }
}
