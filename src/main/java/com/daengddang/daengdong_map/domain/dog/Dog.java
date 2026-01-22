package com.daengddang.daengdong_map.domain.dog;

import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.domain.common.BaseTimeEntity;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "dogs")
@SQLDelete(sql = "UPDATE dogs SET status = 'DELETED', deleted_at = now() WHERE dog_id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "dog_seq_generator",
        sequenceName = "dogs_dog_id_seq",
        allocationSize = 1
)
public class Dog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dog_seq_generator")
    @Column(name = "dog_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 15)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true, length = 10)
    private DogGender gender;

    @Column(name = "is_neutered", nullable = true)
    private boolean isNeutered;

    @Column(name = "weight", nullable = true)
    private Float weight;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private DogStatus status = DogStatus.ACTIVE;

    @Column(name = "dog_key", nullable = false, length = 255, unique = true)
    private String dogKey;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id", nullable = false)
    private Breed breed;

    @Builder
    private Dog(String name,
                LocalDate birthDate,
                DogGender gender,
                boolean isNeutered,
                Float weight,
                String profileImageUrl,
                DogStatus status,
                String dogKey,
                User user,
                Breed breed) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isNeutered = isNeutered;
        this.weight = weight;
        this.profileImageUrl = profileImageUrl;
        this.status = status == null ? DogStatus.ACTIVE : status;
        this.dogKey = dogKey == null ? UUID.randomUUID().toString() : dogKey;
        this.user = user;
        this.breed = breed;
    }

    public void updateProfile(String name, LocalDate birthDate, DogGender gender, boolean isNeutered, Float weight, String profileImageUrl, Breed breed) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isNeutered = isNeutered;
        this.weight = weight;
        this.profileImageUrl = profileImageUrl;
        this.breed = breed;
    }

    public void changeStatus(DogStatus status) {
        this.status = status;
        if (status == DogStatus.DELETED) {
            this.deletedAt = LocalDateTime.now();
        }
    }

}
