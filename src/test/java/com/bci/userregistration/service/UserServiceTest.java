package com.bci.userregistration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.bci.userregistration.dto.PhoneDTO;
import com.bci.userregistration.dto.UserRegistrationRequest;
import com.bci.userregistration.dto.UserResponse;
import com.bci.userregistration.entity.User;
import com.bci.userregistration.exception.DuplicateEmailException;
import com.bci.userregistration.exception.ValidationException;
import com.bci.userregistration.repository.UserRepository;
import com.bci.userregistration.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "emailPattern", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        ReflectionTestUtils.setField(userService, "passwordPattern", "^.{6,}$");
        ReflectionTestUtils.setField(userService, "passwordMessage", "La contraseña debe tener al menos 6 caracteres");
    }

    @Test
    void registerUser_WithValidData_ShouldReturnUserResponse() {
        // Arrange
        UUID userId = UUID.randomUUID();

        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("hunter2")
                .phones(Collections.singletonList(PhoneDTO.builder()
                        .number("1234567")
                        .citycode("1")
                        .contrycode("57")
                        .build()))
                .build();

        // Mock del usuario guardado con todos sus datos
        User savedUser = User.builder()
                .id(userId)
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("hunter2")
                .token("mock-jwt-token")
                .isActive(true)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(anyString(), any(UUID.class))).thenReturn("mock-jwt-token");

        
        UserResponse response = userService.registerUser(request);

        
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("mock-jwt-token", response.getToken());
        assertTrue(response.getIsActive());
        
        verify(userRepository).existsByEmail("juan@rodriguez.org");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(anyString(), any(UUID.class));
    }

    @Test
    void registerUser_WithDuplicateEmail_ShouldThrowDuplicateEmailException() {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("hunter2")
                .phones(Collections.emptyList())
                .build();

        when(userRepository.existsByEmail("juan@rodriguez.org")).thenReturn(true);

        // Act & Assert
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("El correo ya registrado", exception.getMessage());
        verify(userRepository).existsByEmail("juan@rodriguez.org");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithInvalidEmail_ShouldThrowValidationException() {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Juan Rodriguez")
                .email("invalid-email")
                .password("hunter2")
                .phones(Collections.emptyList())
                .build();

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("El formato del correo es inválido", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithShortPassword_ShouldThrowValidationException() {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("123")
                .phones(Collections.emptyList())
                .build();

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("La contraseña debe tener al menos 6 caracteres", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
