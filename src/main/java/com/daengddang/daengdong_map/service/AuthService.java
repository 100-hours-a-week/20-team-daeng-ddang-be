package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.user.UserStatus;
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

    /**
     * Kakao OAuth 결과로 User를 확정한다.
     *
     * - 신규 유저면 최소 정보만으로 생성
     * - 기존 유저면 상태 검증
     * - region 등 프로필 정보는 관여하지 않는다
     */
    @Transactional
    public LoginResult loginOrRegister(KakaoOAuthUser oauthUser) {

        Long kakaoUserId = oauthUser.getKakaoUserId();

        User user = userRepository.findByKakaoUserId(kakaoUserId)
                .orElse(null);
        boolean isNewUser = false;
        if (user == null) {
            user = createNewUser(kakaoUserId);
            isNewUser = true;
        }

        validateUserStatus(user);
        user.updateLastLoginAt(LocalDateTime.now());

        return new LoginResult(user, isNewUser);
    }

    private User createNewUser(Long kakaoUserId) {
        return userRepository.save(
                User.builder()
                        .kakaoUserId(kakaoUserId)
                        .status(UserStatus.ACTIVE)
                        .build()
        );
    }

    private void validateUserStatus(User user) {
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
    }

    public record LoginResult(User user, boolean isNewUser) {}
}
