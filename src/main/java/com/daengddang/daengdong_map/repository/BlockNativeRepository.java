package com.daengddang.daengdong_map.repository;

public interface BlockNativeRepository {

    Long insertIfNotExistsReturningId(int x, int y);
}
