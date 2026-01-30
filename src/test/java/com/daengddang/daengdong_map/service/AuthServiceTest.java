package com.daengddang.daengdong_map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.repository.DogRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DogRepository dogRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginOrRegister_createsUser_whenNotFound() {
        KakaoOAuthUser oauthUser = new KakaoOAuthUser(123L, "user@example.com");

        when(userRepository.findByKakaoUserIdIncludingDeleted(123L)).thenReturn(Optional.empty());
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

        when(userRepository.findByKakaoUserIdIncludingDeleted(123L)).thenReturn(Optional.of(user));

        AuthService.LoginResult result = authService.loginOrRegister(new KakaoOAuthUser(123L, null));

        assertThat(result.isNewUser()).isFalse();
        assertThat(result.user()).isSameAs(user);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginOrRegister_restoresUser_whenUserDeleted() {
        User user = User.builder()
                .kakaoUserId(123L)
                .status(UserStatus.DELETED)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByKakaoUserIdIncludingDeleted(123L)).thenReturn(Optional.of(user));
        when(dogRepository.findByUserIdIncludingDeleted(1L)).thenReturn(Optional.of(Dog.builder().build()));

        AuthService.LoginResult result = authService.loginOrRegister(new KakaoOAuthUser(123L, null));

        assertThat(result.isNewUser()).isFalse();
        assertThat(result.user().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
