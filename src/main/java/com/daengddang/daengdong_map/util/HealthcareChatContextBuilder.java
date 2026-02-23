package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.dto.request.chat.FastApiHealthcareChatRequest;
import com.daengddang.daengdong_map.dto.request.chat.HealthcareChatRequest;
import com.daengddang.daengdong_map.service.chat.session.ChatSession;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import org.springframework.stereotype.Component;

@Component
public class HealthcareChatContextBuilder {

    public FastApiHealthcareChatRequest buildFastApiRequest(Dog dog,
                                                            HealthcareChatRequest request,
                                                            ChatSession session) {
        FastApiHealthcareChatRequest.UserContext userContext =
                FastApiHealthcareChatRequest.UserContext.of(
                        calculateDogAgeYears(dog),
                        toWeightKg(dog.getWeight()),
                        resolveBreedName(dog)
                );

        return FastApiHealthcareChatRequest.of(
                Math.toIntExact(session.getDogId()),
                session.getConversationId(),
                request.getMessage(),
                request.getImageUrl(),
                userContext,
                Collections.emptyList()
        );
    }

    private Integer calculateDogAgeYears(Dog dog) {
        if (dog.getBirthDate() == null) {
            return 0;
        }
        return Math.max(0, Period.between(dog.getBirthDate(), LocalDate.now()).getYears());
    }

    private Integer toWeightKg(Float weight) {
        if (weight == null) {
            return 0;
        }
        return Math.round(weight);
    }

    private String resolveBreedName(Dog dog) {
        if (dog.getBreed() == null || dog.getBreed().getName() == null) {
            return "";
        }
        return dog.getBreed().getName();
    }
}
