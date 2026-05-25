package com.marketplace.config;

import com.marketplace.security.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    public SecurityConfig(OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) {
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<ClientRegistrationRepository> clientRegistrations) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/producto/**", "/categoria/**", "/login", "/register", "/css/**", "/js/**", "/uploads/**", "/sitemap.xml", "/robots.txt").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**", "/products/new", "/products/edit/**", "/products/my", "/products/*/sold", "/products/*/toggle-active", "/products/*/delete").hasAnyRole("USER", "STORE_OWNER", "ADMIN")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        if (clientRegistrations.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(oauth2LoginSuccessHandler)
            );
        }
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
