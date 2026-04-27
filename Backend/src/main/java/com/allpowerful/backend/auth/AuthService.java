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
        User user = userRepository.findByUsername(req.username()).orElseGet(() -> {
            User created = new User();
            created.setUsername(req.username());
            created.setDisplayName(req.username());
            created.setPasswordHash(passwordEncoder.encode(req.password()));
            created.setCreatedAt(LocalDateTime.now());
            created.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(created);
        });
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException("账号或密码错误");
        }
        String token = jwtService.createToken(user.getId(), user.getUsername());
        return new AuthDtos.LoginResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }

    public AuthDtos.MeResponse me(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("用户不存在"));
        return new AuthDtos.MeResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
