package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.dto.response.dog.BreedListResponse;
import com.daengddang.daengdong_map.repository.BreedRepository;
import com.daengddang.daengdong_map.service.cache.BreedCacheMetrics;
import com.daengddang.daengdong_map.service.cache.BreedCacheStore;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BreedService {

    private final BreedRepository breedRepository;
    private final BreedCacheStore breedCacheStore;
    private final BreedCacheMetrics breedCacheMetrics;
    private final AtomicReference<CompletableFuture<BreedListResponse>> inFlightAllBreeds = new AtomicReference<>();

    public BreedListResponse getBreeds(String keyword) {
        if (keyword == null) {
            Optional<BreedListResponse> cached = breedCacheStore.getAll();
            if (cached.isPresent()) {
                return cached.get();
            }

            while (true) {
                CompletableFuture<BreedListResponse> existing = inFlightAllBreeds.get();
                if (existing != null) {
                    return existing.join();
                }

                CompletableFuture<BreedListResponse> created = new CompletableFuture<>();
                if (inFlightAllBreeds.compareAndSet(null, created)) {
                    try {
                        BreedListResponse response = loadAllBreedsFromDbAndCache();
                        created.complete(response);
                        return response;
                    } catch (Exception e) {
                        created.completeExceptionally(e);
                        throw e;
                    } finally {
                        inFlightAllBreeds.compareAndSet(created, null);
                    }
                }
            }
        }

        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            throw new BaseException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        }

        List<Breed> breeds = breedRepository.findByNameContainingOrderByNameAsc(trimmed);
        return BreedListResponse.from(breeds);
    }

    private BreedListResponse loadAllBreedsFromDbAndCache() {
        breedCacheMetrics.recordDbLoad();
        List<Breed> breeds = breedRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        BreedListResponse response = BreedListResponse.from(breeds);
        breedCacheStore.putAll(response);
        return response;
    }
}
