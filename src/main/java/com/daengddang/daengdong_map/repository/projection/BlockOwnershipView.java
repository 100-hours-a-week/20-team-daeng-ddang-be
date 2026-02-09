package com.daengddang.daengdong_map.repository.projection;

import java.time.LocalDateTime;

public interface BlockOwnershipView {

    Long getBlockId();

    Integer getBlockX();

    Integer getBlockY();

    Long getDogId();

    LocalDateTime getAcquiredAt();
}
