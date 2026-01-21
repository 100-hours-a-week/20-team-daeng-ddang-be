package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.region.Region;
import com.daengddang.daengdong_map.domain.region.RegionStatus;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.dog.DogRegisterRequest;
import com.daengddang.daengdong_map.dto.request.dog.DogUpdateRequest;
import com.daengddang.daengdong_map.dto.response.dog.DogRegisterResponse;
import com.daengddang.daengdong_map.dto.response.dog.DogResponse;
import com.daengddang.daengdong_map.dto.response.dog.DogResponse;
import com.daengddang.daengdong_map.repository.BreedRepository;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.RegionRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DogService {

    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final BreedRepository breedRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public DogRegisterResponse registerDog(Long userId, DogRegisterRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        String name = request.getName().trim();
        Long breedId = request.getBreedId();
        Float weight = request.getWeight().floatValue();

        Breed breed = breedRepository.findById(breedId)
                .orElseThrow(() -> new BaseException(ErrorCode.DOG_BREED_NOT_FOUND));

        boolean isNeutered = request.getIsNeutered() != null && request.getIsNeutered();

        Dog dog = Dog.builder()
                .name(name)
                .breed(breed)
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .isNeutered(isNeutered)
                .weight(weight)
                .profileImageUrl(request.getProfileImageUrl())
                .user(user)
                .build();

        Dog saved = dogRepository.save(dog);

        return DogRegisterResponse.of(saved.getId(), saved.getDogKey());
    }

    @Transactional
    public DogResponse getDogInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BaseException(ErrorCode.UNAUTHORIZED)
        );

        Dog dog = dogRepository.findByUser(user).orElse(null);

        if (dog == null) {
            return null;
        }

        return DogResponse.from(dog);
    }

    @Transactional
    public DogResponse updateDogInfo(Long userId, DogUpdateRequest request) {
        if (request == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Dog dog = dogRepository.findByUser(user)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        Breed breed = breedRepository.findById(request.getBreedId())
                .orElseThrow(() -> new BaseException(ErrorCode.DOG_BREED_NOT_FOUND));

        Region region = regionRepository.findByIdAndStatus(request.getRegionId(), RegionStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(ErrorCode.REGION_NOT_FOUND));

        boolean isNeutered = request.getIsNeutered() != null && request.getIsNeutered();

        dog.updateProfile(
                request.getName().trim(),
                request.getBirthDate(),
                request.getGender(),
                isNeutered,
                request.getWeight().floatValue(),
                request.getProfileImageUrl(),
                breed,
                region
        );

        user.updateRegion(region);

        return DogResponse.from(dog);
    }

}
