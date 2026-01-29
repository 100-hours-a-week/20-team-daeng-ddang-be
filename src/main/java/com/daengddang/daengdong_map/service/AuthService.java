package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.user.UserStatus;
import com.daengddang.daengdong_map.repository.DogRepository;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.security.oauth.kakao.model.KakaoOAuthUser;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DogRepository dogRepository;

    /**
     * Kakao OAuth 결과로 User를 확정한다.
     *
     * - 신규 유저면 최소 정보만으로 생성
     * - 기존 유저면 상태 복구/확인
     * - region 등 프로필 정보는 관여하지 않는다
     */
    @Transactional
    public LoginResult loginOrRegister(KakaoOAuthUser oauthUser) {

        Long kakaoUserId = oauthUser.getKakaoUserId();
        String email = oauthUser.getEmail();

        User user = userRepository.findByKakaoUserIdIncludingDeleted(kakaoUserId)
                .orElse(null);
        boolean isNewUser = false;
        if (user == null) {
            user = createNewUser(kakaoUserId, email);
            isNewUser = true;
        }

        if (user.getStatus() == UserStatus.DELETED) {
            restoreUser(user);
            restoreDogIfPresent(user);
        }
        if (email != null && (user.getKakaoEmail() == null || !email.equals(user.getKakaoEmail()))) {
            user.updateKakaoEmail(email);
        }
        user.updateLastLoginAt(LocalDateTime.now());

        return new LoginResult(user, isNewUser);
    }

    private User createNewUser(Long kakaoUserId, String email) {
        return userRepository.save(
                User.builder()
                        .kakaoUserId(kakaoUserId)
                        .kakaoEmail(email)
                        .status(UserStatus.ACTIVE)
                        .build()
        );
    }

    private void restoreUser(User user) {
        user.restore();
    }

    private void restoreDogIfPresent(User user) {
        Dog dog = dogRepository.findByUserIdIncludingDeleted(user.getId()).orElse(null);
        if (dog == null) {
            return;
        }
        dog.restore();
    }

    public record LoginResult(User user, boolean isNewUser) {}
}
