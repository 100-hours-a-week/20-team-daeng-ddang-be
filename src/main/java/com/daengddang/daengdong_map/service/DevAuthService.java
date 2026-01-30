package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.dog.DogGender;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.user.UserStatus;
import com.daengddang.daengdong_map.repository.BreedRepository;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DevAuthService {

    private static final String DEFAULT_PREFIX = "loadtest";
    private static final String DEFAULT_EMAIL_DOMAIN = "example.com";
    private static final LocalDate DEFAULT_DOG_BIRTH_DATE = LocalDate.of(2020, 1, 1);
    private static final float DEFAULT_DOG_WEIGHT = 5.0f;

    private final UserRepository userRepository;
    private final DogRepository dogRepository;
    private final BreedRepository breedRepository;

    /* =======================
       기존 로직
       ======================= */

    @Transactional
    public User getOrCreate(Long kakaoUserId, String nickname, String prefix) {
        long resolvedKakaoUserId = resolveKakaoUserId(kakaoUserId);
        User user = userRepository.findByKakaoUserIdIncludingDeleted(resolvedKakaoUserId)
                .orElseGet(() -> createUser(resolvedKakaoUserId, prefix));

        if (user.getStatus() == UserStatus.DELETED) {
            user.restore();
        }
        user.updateLastLoginAt(LocalDateTime.now());

        ensureDog(user, nickname, prefix);

        return user;
    }

    @Transactional
    public List<Long> seedUsers(int count, String prefix) {
        int target = resolveCount(count);
        String resolvedPrefix = resolvePrefix(prefix);
        Breed breed = resolveBreed();
        AtomicLong kakaoUserIdSequence = new AtomicLong(System.currentTimeMillis() * 1000L);

        List<Long> userIds = new ArrayList<>(target);
        for (int i = 0; i < target; i++) {
            long kakaoUserId = nextKakaoUserId(kakaoUserIdSequence);
            User user = createUser(kakaoUserId, resolvedPrefix);
            createDog(user, breed, buildDogName(resolvedPrefix, i));
            userIds.add(user.getId());
        }

        return userIds;
    }

    @Transactional
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        return userRepository.findByIdIncludingDeleted(userId)
                .map(user -> {
                    if (user.getStatus() == UserStatus.DELETED) {
                        user.restore();
                    }
                    return user;
                })
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /* =======================
       🔥 부하테스트 전용 FAST PATH
       ======================= */

    /**
     * 부하테스트 전용 유저 대량 생성
     * - DB 조회 없음
     * - 유니크만 보장
     * - 현실성보다 속도/안정성 우선
     */
    @Transactional
    public List<Long> seedUsersFast(int count, String prefix) {
        int target = resolveCount(count);
        String resolvedPrefix = resolvePrefix(prefix);
        Breed breed = resolveBreed();

        long baseKakaoUserId = System.currentTimeMillis() * 1000L;

        List<Long> userIds = new ArrayList<>(target);
        for (int i = 0; i < target; i++) {
            long kakaoUserId = baseKakaoUserId + i;

            User user = userRepository.save(
                    User.builder()
                            .kakaoUserId(kakaoUserId)
                            .kakaoEmail(buildEmail(resolvedPrefix, kakaoUserId))
                            .status(UserStatus.ACTIVE)
                            .lastLoginAt(LocalDateTime.now())
                            .build()
            );

            createDog(user, breed, buildDogName(resolvedPrefix, i));
            userIds.add(user.getId());

            // 대량 insert 시 영속성 컨텍스트 안정화
            if (i % 500 == 0) {
                userRepository.flush();
                dogRepository.flush();
            }
        }

        return userIds;
    }

    /* =======================
       내부 헬퍼 메서드
       ======================= */

    private User createUser(long kakaoUserId, String prefix) {
        String resolvedPrefix = resolvePrefix(prefix);
        return userRepository.save(
                User.builder()
                        .kakaoUserId(kakaoUserId)
                        .kakaoEmail(buildEmail(resolvedPrefix, kakaoUserId))
                        .status(UserStatus.ACTIVE)
                        .lastLoginAt(LocalDateTime.now())
                        .build()
        );
    }

    private void ensureDog(User user, String nickname, String prefix) {
        Optional<Dog> existingDog = dogRepository.findByUserIdIncludingDeleted(user.getId());
        if (existingDog.isPresent()) {
            Dog dog = existingDog.get();
            dog.restore();
            return;
        }

        Breed breed = resolveBreed();
        String dogName = resolveDogName(nickname, prefix, user.getId());
        createDog(user, breed, dogName);
    }

    private void createDog(User user, Breed breed, String name) {
        dogRepository.save(
                Dog.builder()
                        .name(name)
                        .birthDate(DEFAULT_DOG_BIRTH_DATE)
                        .gender(DogGender.MALE)
                        .isNeutered(false)
                        .weight(DEFAULT_DOG_WEIGHT)
                        .breed(breed)
                        .user(user)
                        .build()
        );
    }

    private Breed resolveBreed() {
        return breedRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BaseException(ErrorCode.DOG_BREED_NOT_FOUND));
    }

    private int resolveCount(int count) {
        if (count <= 0 || count > 20000) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
        return count;
    }

    private long resolveKakaoUserId(Long kakaoUserId) {
        if (kakaoUserId != null) {
            return kakaoUserId;
        }
        AtomicLong sequence = new AtomicLong(System.currentTimeMillis() * 1000L);
        return nextKakaoUserId(sequence);
    }

    private long nextKakaoUserId(AtomicLong sequence) {
        long candidate = sequence.getAndIncrement();
        while (userRepository.findByKakaoUserIdIncludingDeleted(candidate).isPresent()) {
            candidate = sequence.getAndIncrement();
        }
        return candidate;
    }

    private String resolvePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return DEFAULT_PREFIX;
        }
        return sanitizePrefix(prefix);
    }

    private String resolveDogName(String nickname, String prefix, long seed) {
        if (nickname != null && !nickname.isBlank()) {
            return trimDogName(sanitizePrefix(nickname));
        }
        String resolvedPrefix = resolvePrefix(prefix);
        return buildDogName(resolvedPrefix, (int) (seed % 26));
    }

    private String sanitizePrefix(String raw) {
        String onlyLetters = raw.replaceAll("[^A-Za-z가-힣]", "");
        if (onlyLetters.isBlank()) {
            return DEFAULT_PREFIX;
        }
        return onlyLetters;
    }

    private String buildEmail(String prefix, long kakaoUserId) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return normalized + "+" + kakaoUserId + "@" + DEFAULT_EMAIL_DOMAIN;
    }

    private String buildDogName(String prefix, int index) {
        String suffix = toAlphaSuffix(index);
        String safePrefix = trimDogName(prefix);
        int maxPrefixLength = Math.max(1, 15 - suffix.length());
        if (safePrefix.length() > maxPrefixLength) {
            safePrefix = safePrefix.substring(0, maxPrefixLength);
        }
        return safePrefix + suffix;
    }

    private String trimDogName(String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_PREFIX;
        }
        return name.length() > 15 ? name.substring(0, 15) : name;
    }

    private String toAlphaSuffix(int index) {
        int value = index + 1;
        StringBuilder builder = new StringBuilder();
        while (value > 0) {
            int mod = (value - 1) % 26;
            builder.append((char) ('A' + mod));
            value = (value - 1) / 26;
        }
        return builder.reverse().toString();
    }
}