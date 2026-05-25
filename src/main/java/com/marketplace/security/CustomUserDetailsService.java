package com.marketplace.security;

import com.marketplace.entity.User;
import com.marketplace.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword() == null ? "" : user.getPassword(),
            user.isEnabled(),
            true,
            true,
            !user.isBlocked(),
            java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
