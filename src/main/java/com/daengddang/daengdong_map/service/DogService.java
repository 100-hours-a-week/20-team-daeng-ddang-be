package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.breed.Breed;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.dto.request.dog.DogRegisterRequest;
import com.daengddang.daengdong_map.dto.request.dog.DogUpdateRequest;
import com.daengddang.daengdong_map.dto.response.dog.DogRegisterResponse;
import com.daengddang.daengdong_map.dto.response.dog.DogResponse;
import com.daengddang.daengdong_map.repository.BreedRepository;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.util.AccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DogService {

    private final DogRepository dogRepository;
    private final BreedRepository breedRepository;
    private final AccessValidator accessValidator;

    @Transactional
    public DogRegisterResponse registerDog(Long userId, DogRegisterRequest dto) {

        User user = accessValidator.getUserOrThrow(userId);

        Long breedId = dto.getBreedId();

        Breed breed = breedRepository.findById(breedId)
                .orElseThrow(() -> new BaseException(ErrorCode.DOG_BREED_NOT_FOUND));

        Dog dog = DogRegisterRequest.of(dto, user, breed);

        Dog saved = dogRepository.save(dog);

        return DogRegisterResponse.from(saved.getId(), saved.getDogKey());
    }

    @Transactional
    public DogResponse getDogInfo(Long userId) {
        Dog dog = dogRepository.findByUser(accessValidator.getUserOrThrow(userId)).orElse(null);

        if (dog == null) {
            return null;
        }

        return DogResponse.from(dog);
    }

    @Transactional
    public DogResponse updateDogInfo(Long userId, DogUpdateRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        Dog dog = accessValidator.getDogOrThrow(userId);

        Breed breed = breedRepository.findById(dto.getBreedId())
                .orElseThrow(() -> new BaseException(ErrorCode.DOG_BREED_NOT_FOUND));

        DogUpdateRequest.of(dto, dog, breed);

        return DogResponse.from(dog);
    }

}
