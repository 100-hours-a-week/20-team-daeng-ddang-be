package com.daengddang.daengdong_map.service.ranking.zset;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.repository.DogRepository;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingDogMetaCacheService {

    private static final String META_FIELD_SEP = ".";

    private final RankingZsetKeyFactory keyFactory;
    private final RedissonClient redissonClient;
    private final DogRepository dogRepository;

    public Map<Long, DogMeta> getByDogIds(Collection<Long> dogIds) {
        if (dogIds == null || dogIds.isEmpty()) {
            return Map.of();
        }

        Set<Long> uniqueDogIds = new LinkedHashSet<>(dogIds);
        Set<String> dogIdKeys = uniqueDogIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.toSet());

        RMap<String, String> packedMap = redissonClient.getMap(keyFactory.dogMetaPackedMapKey(), StringCodec.INSTANCE);
        Map<String, String> packedValues = packedMap.getAll(dogIdKeys);

        Map<Long, DogMeta> result = new LinkedHashMap<>(uniqueDogIds.size());
        Set<Long> misses = new LinkedHashSet<>();
        for (Long dogId : uniqueDogIds) {
            String key = String.valueOf(dogId);
            DogMeta unpacked = unpack(packedValues.get(key));
            if (unpacked == null || unpacked.name() == null) {
                misses.add(dogId);
                continue;
            }
            result.put(dogId, unpacked);
        }

        if (misses.isEmpty()) {
            return result;
        }

        try {
            Map<String, String> updatePacked = new LinkedHashMap<>();

            for (Dog dog : dogRepository.findAllWithBreedByIdIn(misses)) {
                String key = String.valueOf(dog.getId());
                String name = valueOrEmpty(dog.getName());
                String birthDate = dog.getBirthDate() == null ? "" : dog.getBirthDate().toString();
                String profileImageUrl = valueOrEmpty(dog.getProfileImageUrl());
                String breed = dog.getBreed() == null ? "" : valueOrEmpty(dog.getBreed().getName());

                updatePacked.put(key, pack(name, birthDate, profileImageUrl, breed));

                result.put(
                        dog.getId(),
                        new DogMeta(
                                nullIfBlank(name),
                                parseDate(nullIfBlank(birthDate)),
                                nullIfBlank(profileImageUrl),
                                nullIfBlank(breed)
                        )
                );
            }

            if (!updatePacked.isEmpty()) {
                packedMap.putAll(updatePacked);
            }
        } catch (Exception e) {
            log.warn("랭킹 dog 메타 캐시 적재 실패 (Ranking dog meta cache fill failed)", e);
        }

        return result;
    }

    public void upsert(Dog dog) {
        if (dog == null || dog.getId() == null) {
            return;
        }
        try {
            String key = String.valueOf(dog.getId());
            String packed = pack(
                    valueOrEmpty(dog.getName()),
                    dog.getBirthDate() == null ? "" : dog.getBirthDate().toString(),
                    valueOrEmpty(dog.getProfileImageUrl()),
                    dog.getBreed() == null ? "" : valueOrEmpty(dog.getBreed().getName())
            );
            redissonClient.getMap(keyFactory.dogMetaPackedMapKey(), StringCodec.INSTANCE)
                    .put(key, packed);
        } catch (Exception e) {
            log.warn("랭킹 dog 메타 캐시 갱신 실패 (Ranking dog meta cache upsert failed): dogId={}", dog.getId(), e);
        }
    }

    private DogMeta unpack(String packed) {
        if (packed == null || packed.isBlank()) {
            return null;
        }
        String[] parts = packed.split("\\.", -1);
        if (parts.length != 4) {
            return null;
        }
        String name = nullIfBlank(fromBase64(parts[0]));
        return new DogMeta(
                name,
                parseDate(nullIfBlank(fromBase64(parts[1]))),
                nullIfBlank(fromBase64(parts[2])),
                nullIfBlank(fromBase64(parts[3]))
        );
    }

    private String pack(String name, String birthDate, String profileImageUrl, String breed) {
        return toBase64(name) + META_FIELD_SEP
                + toBase64(birthDate) + META_FIELD_SEP
                + toBase64(profileImageUrl) + META_FIELD_SEP
                + toBase64(breed);
    }

    private String toBase64(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valueOrEmpty(value).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String fromBase64(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(value), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public record DogMeta(String name, LocalDate birthDate, String profileImageUrl, String breed) {
    }
}
