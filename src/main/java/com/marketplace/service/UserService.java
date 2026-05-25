package com.marketplace.service;

import com.marketplace.dto.RegisterForm;
import com.marketplace.dto.ProfileForm;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.exception.NotFoundException;
import com.marketplace.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAvatarStorage userAvatarStorage;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserAvatarStorage userAvatarStorage) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAvatarStorage = userAvatarStorage;
    }

    @Transactional
    public User register(RegisterForm form) {
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }
        User user = new User();
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setPhone(form.getPhone());
        user.setCity(form.getCity());
        user.setProvince(form.getProvince());
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setBlocked(false);
        return userRepository.save(user);
    }

    public User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Transactional
    public void setBlocked(Long id, boolean blocked) {
        User user = get(id);
        user.setBlocked(blocked);
    }

    @Transactional
    public void verify(Long id) {
        User user = get(id);
        user.setVerified(true);
    }

    @Transactional
    public void updateProfile(User user, ProfileForm form, MultipartFile avatar) {
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setPhone(form.getPhone());
        user.setCity(form.getCity());
        user.setProvince(form.getProvince());
        user.setAvatarUrl(userAvatarStorage.replace(avatar, user.getAvatarUrl()));
        userRepository.save(user);
    }

    @Transactional
    public void updateAvatar(User user, MultipartFile avatar) {
        user.setAvatarUrl(userAvatarStorage.replace(avatar, user.getAvatarUrl()));
        userRepository.save(user);
    }
}
