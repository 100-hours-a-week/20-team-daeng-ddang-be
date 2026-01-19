package com.daengddang.daengdong_map.domain.breed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "breed")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "breed_seq_generator",
        sequenceName = "breed_breed_id_seq",
        allocationSize = 1
)
public class Breed {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "breed_seq_generator")
    @Column(name = "breed_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Builder
    private Breed(String name) {
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }
}
