package com.nghoang.banking.unit.service;

import com.nghoang.banking.entity.User;
import com.nghoang.banking.repository.UserRepository;
import com.nghoang.banking.service.impl.UserService;
import com.nghoang.banking.service.impl.UserServiceImpl;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @BeforeEach
    void setUp() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("A");
        mockUser.setLastName("Lê");
        mockUser.setEmail("lea@gmail.com");


    }

}
