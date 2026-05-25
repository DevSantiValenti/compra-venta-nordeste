package com.marketplace.config;

import com.marketplace.entity.Category;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.CategoryRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.util.SlugUtil;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(CategoryRepository categoryRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (categoryRepository.count() == 0) {
                List<String> names = List.of("Bicicletas", "Componentes", "Ruedas y cubiertas", "Indumentaria", "Accesorios", "Herramientas", "Entrenamiento");
                for (int i = 0; i < names.size(); i++) {
                    Category category = new Category();
                    category.setName(names.get(i));
                    category.setSlug(SlugUtil.slugify(names.get(i)));
                    category.setIcon("circle");
                    category.setDisplayOrder(i);
                    categoryRepository.save(category);
                }
            }
            if (!userRepository.existsByEmailIgnoreCase("admin@ciclismonea.local")) {
                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("NEA");
                admin.setEmail("admin@ciclismonea.local");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setPhone("3790000000");
                admin.setCity("Corrientes");
                admin.setProvince("Corrientes");
                admin.setRole(Role.ADMIN);
                admin.setVerified(true);
                userRepository.save(admin);
            }
        };
    }
}
