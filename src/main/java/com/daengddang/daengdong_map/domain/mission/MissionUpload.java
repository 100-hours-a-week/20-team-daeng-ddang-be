package com.daengddang.daengdong_map.domain.mission;

import com.daengddang.daengdong_map.domain.walk.Walk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
        name = "mission_uploads",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mission_uploads_walk_mission",
                        columnNames = {"walk_id", "mission_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "mission_upload_seq_generator",
        sequenceName = "mission_uploads_mission_upload_id_seq",
        allocationSize = 1
)
public class MissionUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mission_upload_seq_generator")
    @Column(name = "mission_upload_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id", nullable = false)
    private Walk walk;

    @Column(name = "video_url", nullable = false, length = 1000)
    private String videoUrl;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Builder
    private MissionUpload(Mission mission, Walk walk, String videoUrl, LocalDateTime uploadedAt) {
        this.mission = mission;
        this.walk = walk;
        this.videoUrl = videoUrl;
        this.uploadedAt = uploadedAt;
    }

    @PrePersist
    private void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    private void onUpdate() {
        uploadedAt = LocalDateTime.now();
    }

    public void updateVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
