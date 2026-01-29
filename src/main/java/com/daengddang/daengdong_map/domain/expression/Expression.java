package com.daengddang.daengdong_map.domain.expression;

import com.daengddang.daengdong_map.domain.dog.Dog;
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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "expressions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "expression_seq_generator",
        sequenceName = "expressions_expression_id_seq",
        allocationSize = 1
)
public class Expression {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expression_seq_generator")
    @Column(name = "expression_id")
    private Long id;

    @Column(name = "video_url", nullable = false, length = 1000)
    private String videoUrl;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "predicted_emotion", nullable = false, length = 50)
    private String predictedEmotion;

    @Column(name = "angry", nullable = false)
    private Double angry;

    @Column(name = "happy", nullable = false)
    private Double happy;

    @Column(name = "sad", nullable = false)
    private Double sad;

    @Column(name = "relaxed", nullable = false)
    private Double relaxed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id", nullable = false)
    private Walk walk;

    @Builder
    private Expression(String videoUrl,
                       String summary,
                       String predictedEmotion,
                       Double angry,
                       Double happy,
                       Double sad,
                       Double relaxed,
                       LocalDateTime createdAt,
                       Dog dog,
                       Walk walk) {
        this.videoUrl = videoUrl;
        this.summary = summary;
        this.predictedEmotion = predictedEmotion;
        this.angry = angry;
        this.happy = happy;
        this.sad = sad;
        this.relaxed = relaxed;
        this.createdAt = createdAt;
        this.dog = dog;
        this.walk = walk;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void updateResult(String videoUrl,
                             String summary,
                             String predictedEmotion,
                             Double angry,
                             Double happy,
                             Double sad,
                             Double relaxed) {
        this.videoUrl = videoUrl;
        this.summary = summary;
        this.predictedEmotion = predictedEmotion;
        this.angry = angry;
        this.happy = happy;
        this.sad = sad;
        this.relaxed = relaxed;
    }
}
