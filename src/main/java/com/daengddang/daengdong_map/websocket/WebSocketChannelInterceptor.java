package com.daengddang.daengdong_map.websocket;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.user.User;
import com.daengddang.daengdong_map.domain.user.UserStatus;
import com.daengddang.daengdong_map.repository.UserRepository;
import com.daengddang.daengdong_map.security.AuthUser;
import com.daengddang.daengdong_map.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT != accessor.getCommand()) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION.toLowerCase());
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            Long userId = Long.valueOf(claims.getSubject());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BaseException(ErrorCode.UNAUTHORIZED);
            }

            AuthUser authUser = new AuthUser(user.getId(), user.getStatus());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authUser,
                            null,
                            authUser.getAuthorities()
                    );

            accessor.setUser(authentication);
            return message;
        } catch (Exception e) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
    }
}
