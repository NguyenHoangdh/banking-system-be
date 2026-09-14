package com.nghoang.banking.config;

import com.nghoang.banking.entity.Role;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.repository.RoleRepository;
import com.nghoang.banking.repository.UserRepository;
import com.nghoang.banking.utils.AccountUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            var roleAdmin = roleRepository.findById("ADMIN").orElseGet(() ->
                    roleRepository.save(Role.builder()
                            .name("ADMIN")
                            .description("Admin role")
                            .build()));
            if (!userRepository.existsByEmailIs("admin@gmail.com")) {
                User user = User.builder()
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .accountBalance(BigDecimal.ZERO)
                        .status("ACTIVE")
                        .accountNumber(AccountUtils.generateAccountNumber())
                        .roles(new HashSet<>(Set.of(roleAdmin)))
                        .build();
                userRepository.save(user);
                log.warn("Default admin has been created with emails: admin@gmail.com, password: 123456");
            }
        };
    }
}
