package com.daengddang.daengdong_map.domain.dog;

import lombok.Getter;

@Getter
public enum DogGender {
    MALE("수컷"),
    FEMALE("암컷");

    private final String displayName;

    DogGender(String displayName) {
        this.displayName = displayName;
    }
}
