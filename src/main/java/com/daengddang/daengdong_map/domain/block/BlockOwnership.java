package com.daengddang.daengdong_map.domain.block;

import com.daengddang.daengdong_map.domain.dog.Dog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "block_ownership")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockOwnership {

    @Id
    @Column(name = "block_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "block_id")
    private Block block;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "last_passed_at", nullable = false)
    private LocalDateTime lastPassedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @Builder
    private BlockOwnership(Block block,
                           LocalDateTime acquiredAt,
                           LocalDateTime lastPassedAt,
                           Dog dog) {
        this.block = block;
        this.acquiredAt = acquiredAt;
        this.lastPassedAt = lastPassedAt;
        this.dog = dog;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (acquiredAt == null) {
            acquiredAt = now;
        }
        if (lastPassedAt == null) {
            lastPassedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateOwner(Dog dog, LocalDateTime passedAt) {
        this.dog = dog;
        this.lastPassedAt = passedAt;
        this.acquiredAt = passedAt;
    }

    public void updateLastPassedAt(LocalDateTime passedAt) {
        this.lastPassedAt = passedAt;
    }
}
