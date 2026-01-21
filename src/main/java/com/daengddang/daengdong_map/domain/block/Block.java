package com.daengddang.daengdong_map.domain.block;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@Table(name = "blocks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_blocks_xy", columnNames = {"x", "y"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "block_seq_generator",
        sequenceName = "blocks_block_id_seq",
        allocationSize = 1
)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "block_seq_generator")
    @Column(name = "block_id")
    private Long id;

    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Block(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
