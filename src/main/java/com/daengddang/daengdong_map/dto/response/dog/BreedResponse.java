package com.daengddang.daengdong_map.dto.response.dog;

import com.daengddang.daengdong_map.domain.breed.Breed;
import lombok.Getter;

@Getter
public class BreedResponse {

    private final Long breedId;
    private final String name;

    private BreedResponse(Long breedId, String name) {
        this.breedId = breedId;
        this.name = name;
    }

    public static BreedResponse from(Breed breed) {
        return new BreedResponse(breed.getId(), breed.getName());
    }

    public static BreedResponse of(Long breedId, String name) {
        return new BreedResponse(breedId, name);
    }
}
