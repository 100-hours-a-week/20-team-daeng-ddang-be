package com.daengddang.daengdong_map.service.ranking.zset;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.repository.DogRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RankingDogMetaCacheService {

    private static final char META_FIELD_SEP = '|';

    private final RankingZsetKeyFactory keyFactory;
    private final DogRepository dogRepository;
    private final RMap<String, String> packedMap;

    public RankingDogMetaCacheService(RankingZsetKeyFactory keyFactory,
                                      RedissonClient redissonClient,
                                      DogRepository dogRepository) {
        this.keyFactory = keyFactory;
        this.dogRepository = dogRepository;
        this.packedMap = redissonClient.getMap(keyFactory.dogMetaPackedMapKey(), StringCodec.INSTANCE);
    }

    public Map<Long, DogMeta> getByDogIds(Collection<Long> dogIds) {
        if (dogIds == null || dogIds.isEmpty()) {
            return Map.of();
        }

        Set<Long> uniqueDogIds = new LinkedHashSet<>(dogIds);
        Set<String> dogIdKeys = new LinkedHashSet<>(uniqueDogIds.size());
        for (Long id : uniqueDogIds) {
            dogIdKeys.add(String.valueOf(id));
        }

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
            Map<String, String> updatePacked = new LinkedHashMap<>(misses.size());

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
            packedMap.put(key, packed);
        } catch (Exception e) {
            log.warn("랭킹 dog 메타 캐시 갱신 실패 (Ranking dog meta cache upsert failed): dogId={}", dog.getId(), e);
        }
    }

    private DogMeta unpack(String packed) {
        if (packed == null || packed.isBlank()) {
            return null;
        }

        int firstSep = packed.indexOf(META_FIELD_SEP);
        if (firstSep < 0) {
            return null;
        }
        int secondSep = packed.indexOf(META_FIELD_SEP, firstSep + 1);
        if (secondSep < 0) {
            return null;
        }
        int thirdSep = packed.indexOf(META_FIELD_SEP, secondSep + 1);
        if (thirdSep < 0) {
            return null;
        }
        if (packed.indexOf(META_FIELD_SEP, thirdSep + 1) >= 0) {
            return null;
        }

        String name = nullIfBlank(packed.substring(0, firstSep));
        String birthDate = nullIfBlank(packed.substring(firstSep + 1, secondSep));
        String profileImageUrl = nullIfBlank(packed.substring(secondSep + 1, thirdSep));
        String breed = nullIfBlank(packed.substring(thirdSep + 1));

        return new DogMeta(
                name,
                parseDate(birthDate),
                profileImageUrl,
                breed
        );
    }

    private String pack(String name, String birthDate, String profileImageUrl, String breed) {
        return valueOrEmpty(name) + META_FIELD_SEP
                + valueOrEmpty(birthDate) + META_FIELD_SEP
                + valueOrEmpty(profileImageUrl) + META_FIELD_SEP
                + valueOrEmpty(breed);
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
