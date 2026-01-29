package com.daengddang.daengdong_map.domain.diary;

import com.daengddang.daengdong_map.domain.common.BaseTimeEntity;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.walk.Walk;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "walk_diaries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "walk_diary_seq_generator",
        sequenceName = "walk_diaries_walk_diary_id_seq",
        allocationSize = 1
)
public class WalkDiary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "walk_diary_seq_generator")
    @Column(name = "walk_diary_id")
    private Long id;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "map_image_url", nullable = true, length = 255)
    private String mapImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id", nullable = false)
    private Walk walk;


    @Builder
    private WalkDiary(String memo,
                      String mapImageUrl,
                      User user,
                      Walk walk) {
        this.memo = memo;
        this.mapImageUrl = mapImageUrl;
        this.user = user;
        this.walk = walk;}
}
