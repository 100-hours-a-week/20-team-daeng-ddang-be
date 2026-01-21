package com.daengddang.daengdong_map.domain.user;

import com.daengddang.daengdong_map.domain.common.BaseTimeEntity;
import com.daengddang.daengdong_map.domain.region.Region;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "user_seq_generator",
        sequenceName = "users_user_id_seq",
        allocationSize = 1
)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_generator")
    @Column(name = "user_id")
    private Long id;

    @Column(name = "kakao_user_id", nullable = false, unique = true)
    private Long kakaoUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at", nullable = true)
    private LocalDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = true)
    private Region region;

    @Builder
    private User(Long kakaoUserId,
                 UserStatus status,
                 LocalDateTime lastLoginAt,
                 Region region) {
        this.kakaoUserId = kakaoUserId;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.region = region;
    }

    public void updateRegion(Region region) {
        this.region = region;
    }

    public void updateLastLoginAt(LocalDateTime loginAt) {
        this.lastLoginAt = loginAt;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        this.status = UserStatus.DELETED;
        this.deletedAt = deletedAt;
    }
}
