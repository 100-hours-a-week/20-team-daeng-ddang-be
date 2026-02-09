package com.daengddang.daengdong_map.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class BlockRepositoryImpl implements BlockNativeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Long insertIfNotExistsReturningId(int x, int y) {
        Query query = entityManager.createNativeQuery("""
                INSERT INTO blocks (x, y, created_at)
                VALUES (:x, :y, NOW())
                ON CONFLICT (x, y) DO UPDATE
                SET created_at = blocks.created_at
                RETURNING block_id
                """);
        query.setParameter("x", x);
        query.setParameter("y", y);
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }
}
