package com.daengddang.daengdong_map.dto.response.dog;

import lombok.Getter;

@Getter
public class DogRegisterResponse {

    private final Long dogId;
    private final String dogKey;

    private DogRegisterResponse(Long dogId, String dogKey) {
        this.dogId = dogId;
        this.dogKey = dogKey;
    }

    public static DogRegisterResponse from(Long dogId, String dogKey) {
        return new DogRegisterResponse(dogId, dogKey);
    }
}
