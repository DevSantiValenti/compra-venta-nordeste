package com.marketplace.service;

import com.marketplace.entity.User;
import com.marketplace.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User && oauth2User.getAttribute("email") != null) {
            return userRepository.findByEmailIgnoreCase(oauth2User.getAttribute("email"));
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }

    public User requireUser() {
        return currentUser().orElseThrow(() -> new IllegalStateException("Usuario no autenticado"));
    }
}
