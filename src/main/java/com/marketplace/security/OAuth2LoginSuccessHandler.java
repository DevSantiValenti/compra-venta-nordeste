package com.marketplace.security;

import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect("/login?error");
            return;
        }
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> createUser(oauthUser, email));
        if (user.isBlocked() || !user.isEnabled()) {
            response.sendRedirect("/login?blocked");
            return;
        }
        response.sendRedirect("/");
    }

    private User createUser(OAuth2User oauthUser, String email) {
        String name = oauthUser.getAttribute("name");
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstPart(name));
        user.setLastName(lastPart(name));
        user.setPhone("");
        user.setCity("");
        user.setProvince("");
        user.setAvatarUrl(oauthUser.getAttribute("picture"));
        user.setRole(Role.USER);
        user.setVerified(true);
        return userRepository.save(user);
    }

    private String firstPart(String name) {
        if (name == null || name.isBlank()) {
            return "Usuario";
        }
        return name.trim().split("\\s+")[0];
    }

    private String lastPart(String name) {
        if (name == null || name.isBlank() || !name.trim().contains(" ")) {
            return "";
        }
        return name.trim().substring(name.trim().indexOf(' ') + 1);
    }
}
