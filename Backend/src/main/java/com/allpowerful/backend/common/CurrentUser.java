package com.allpowerful.backend.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new AppException("未认证");
        }
        String value = String.valueOf(auth.getPrincipal());
        if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User user) {
            value = user.getUsername();
        }
        return Long.parseLong(value);
    }
}
