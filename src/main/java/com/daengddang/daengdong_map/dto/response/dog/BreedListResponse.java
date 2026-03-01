package com.daengddang.daengdong_map.dto.response.dog;

import com.daengddang.daengdong_map.domain.breed.Breed;
import java.util.List;
import lombok.Getter;

@Getter
public class BreedListResponse {

    private final List<BreedResponse> breeds;

    private BreedListResponse(List<BreedResponse> breeds) {
        this.breeds = breeds;
    }

    public static BreedListResponse from(List<Breed> breeds) {
        List<BreedResponse> responses = breeds.stream()
                .map(BreedResponse::from)
                .toList();
        return new BreedListResponse(responses);
    }

    public static BreedListResponse of(List<BreedResponse> breeds) {
        return new BreedListResponse(breeds);
    }
}
