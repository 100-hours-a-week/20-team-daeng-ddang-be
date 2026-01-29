package com.daengddang.daengdong_map.dto.request.dog;

import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.dog.DogGender;
import com.daengddang.daengdong_map.domain.user.User;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class DogRegisterRequest {

    @NotBlank
    @Size(min = 2, max = 15)
    @Pattern(regexp = "^[A-Za-z가-힣]+$")
    private String name;

    @NotNull
    private Long breedId;

    private LocalDate birthDate;

    private DogGender gender;
    private Boolean isNeutered;

    @NotNull
    @Digits(integer = 5, fraction = 1)
    private BigDecimal weight;

    @Size(max = 255)
    private String profileImageUrl;

    public static Dog of(DogRegisterRequest dto, User user, Breed breed) {
        String name = dto.getName() == null ? null : dto.getName().trim();
        boolean isNeutered = Boolean.TRUE.equals(dto.getIsNeutered());
        Float weight = dto.getWeight() == null ? null : dto.getWeight().floatValue();

        return Dog.builder()
                .name(name)
                .breed(breed)
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .isNeutered(isNeutered)
                .weight(weight)
                .profileImageUrl(dto.getProfileImageUrl())
                .user(user)
                .build();
    }
}
