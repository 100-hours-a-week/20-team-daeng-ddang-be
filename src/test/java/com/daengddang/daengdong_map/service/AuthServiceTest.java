package com.daengddang.daengdong_map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.user.UserStatus;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.security.oauth.kakao.model.KakaoOAuthUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginOrRegister_createsUser_whenNotFound() {
        KakaoOAuthUser oauthUser = new KakaoOAuthUser(123L, "user@example.com");

        when(userRepository.findByKakaoUserId(123L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthService.LoginResult result = authService.loginOrRegister(oauthUser);

        assertThat(result.isNewUser()).isTrue();
        assertThat(result.user().getKakaoUserId()).isEqualTo(123L);
        assertThat(result.user().getKakaoEmail()).isEqualTo("user@example.com");
        assertThat(result.user().getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginOrRegister_returnsExistingUser_whenActive() {
        User user = User.builder()
                .kakaoUserId(123L)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByKakaoUserId(123L)).thenReturn(Optional.of(user));

        AuthService.LoginResult result = authService.loginOrRegister(new KakaoOAuthUser(123L, null));

        assertThat(result.isNewUser()).isFalse();
        assertThat(result.user()).isSameAs(user);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginOrRegister_throws_whenUserDeleted() {
        User user = User.builder()
                .kakaoUserId(123L)
                .status(UserStatus.DELETED)
                .build();

        when(userRepository.findByKakaoUserId(123L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.loginOrRegister(new KakaoOAuthUser(123L, null)))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
