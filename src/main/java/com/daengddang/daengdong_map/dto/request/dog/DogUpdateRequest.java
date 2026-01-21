package com.daengddang.daengdong_map.dto.request.dog;

import com.daengddang.daengdong_map.domain.dog.DogGender;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class DogUpdateRequest {

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
}
