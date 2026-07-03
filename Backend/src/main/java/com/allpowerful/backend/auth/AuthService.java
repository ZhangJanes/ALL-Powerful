package com.allpowerful.backend.auth;

import com.allpowerful.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new AppException("账号或密码错误"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException("账号或密码错误");
        }
        return toLoginResponse(user);
    }

    @Transactional
    public AuthDtos.LoginResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new AppException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setDisplayName(
                req.displayName() == null || req.displayName().isBlank() ? req.username() : req.displayName().trim()
        );
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return toLoginResponse(user);
    }

    private AuthDtos.LoginResponse toLoginResponse(User user) {
        String token = jwtService.createToken(user.getId(), user.getUsername());
        return new AuthDtos.LoginResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }

    public AuthDtos.MeResponse me(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("用户不存在"));
        return new AuthDtos.MeResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
