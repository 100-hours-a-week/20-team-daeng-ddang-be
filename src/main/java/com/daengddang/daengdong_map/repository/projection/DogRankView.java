package com.daengddang.daengdong_map.repository.projection;

import java.time.LocalDate;

public interface DogRankView {

    Integer getRank();

    Long getDogId();

    String getDogName();

    LocalDate getBirthDate();

    String getProfileImageUrl();

    Double getTotalDistance();
}
