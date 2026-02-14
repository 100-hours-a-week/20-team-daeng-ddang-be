package com.daengddang.daengdong_map.domain.analysis;

import com.daengddang.daengdong_map.domain.dog.Dog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "healthcare_analysis_seq_generator",
        sequenceName = "analysis_analysis_id_seq",
        allocationSize = 1
)
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "healthcare_analysis_seq_generator")
    @Column(name = "analysis_id")
    private Long id;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "risk_level", nullable = false, length = 10)
    private String riskLevel;

    @Column(name = "video_url", nullable = false, length = 512)
    private String videoUrl;

    @Column(name = "patella_risk_score", nullable = false)
    private Integer patellaRiskScore;

    @Column(name = "patella_risk_desc", nullable = false)
    private String patellaRiskDesc;

    @Column(name = "gait_balance_score", nullable = false)
    private Integer gaitBalanceScore;

    @Column(name = "gait_balance_desc", nullable = false)
    private String gaitBalanceDesc;

    @Column(name = "knee_mobility_score", nullable = false)
    private Integer kneeMobilityScore;

    @Column(name = "knee_mobility_desc", nullable = false)
    private String kneeMobilityDesc;

    @Column(name = "gait_stability_score", nullable = false)
    private Integer gaitStabilityScore;

    @Column(name = "gait_stability_desc", nullable = false)
    private String gaitStabilityDesc;

    @Column(name = "gait_rhythm_score", nullable = false)
    private Integer gaitRhythmScore;

    @Column(name = "gait_rhythm_desc", nullable = false)
    private String gaitRhythmDesc;

    @Column(name = "keypoint_overlay_video_url", length = 512)
    private String keypointOverlayVideoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @Builder
    private Analysis(String summary,
                     String riskLevel,
                     String videoUrl,
                     Integer patellaRiskScore,
                     String patellaRiskDesc,
                     Integer gaitBalanceScore,
                     String gaitBalanceDesc,
                     Integer kneeMobilityScore,
                     String kneeMobilityDesc,
                     Integer gaitStabilityScore,
                     String gaitStabilityDesc,
                     Integer gaitRhythmScore,
                     String gaitRhythmDesc,
                     String keypointOverlayVideoUrl,
                     LocalDateTime createdAt,
                     Dog dog) {
        this.summary = summary;
        this.riskLevel = riskLevel;
        this.videoUrl = videoUrl;
        this.patellaRiskScore = patellaRiskScore;
        this.patellaRiskDesc = patellaRiskDesc;
        this.gaitBalanceScore = gaitBalanceScore;
        this.gaitBalanceDesc = gaitBalanceDesc;
        this.kneeMobilityScore = kneeMobilityScore;
        this.kneeMobilityDesc = kneeMobilityDesc;
        this.gaitStabilityScore = gaitStabilityScore;
        this.gaitStabilityDesc = gaitStabilityDesc;
        this.gaitRhythmScore = gaitRhythmScore;
        this.gaitRhythmDesc = gaitRhythmDesc;
        this.keypointOverlayVideoUrl = keypointOverlayVideoUrl;
        this.createdAt = createdAt;
        this.dog = dog;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
