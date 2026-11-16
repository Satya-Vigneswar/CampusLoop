package com.campusloop.campusloop.service;

import com.campusloop.campusloop.controller.RegistrationForm;
import com.campusloop.campusloop.entity.User;
import com.campusloop.campusloop.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean register(RegistrationForm form) {
        String email = normalizeEmail(form.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return false;
        }

        User user = new User(
                form.getFullName().trim(),
                email,
                passwordEncoder.encode(form.getPassword()),
                form.getCollegeName().trim(),
                form.getDepartment().trim());
        userRepository.save(user);
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
