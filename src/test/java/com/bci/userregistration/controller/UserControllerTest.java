package com.bci.userregistration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bci.userregistration.dto.PhoneDTO;
import com.bci.userregistration.dto.UserRegistrationRequest;
import com.bci.userregistration.dto.UserResponse;
import com.bci.userregistration.exception.DuplicateEmailException;
import com.bci.userregistration.exception.ValidationException;
import com.bci.userregistration.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    @Test
    void registerUser_WithValidData_ShouldReturnCreated() throws Exception {

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

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .created(new Date())
                .modified(new Date())
                .lastLogin(new Date())
                .token("mock-jwt-token")
                .isActive(true)
                .build();

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.isactive").value(true));
    }

    @Test
    void registerUser_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Juan Rodriguez")
                .email("juan@rodriguez.org")
                .password("hunter2")
                .phones(Collections.emptyList())
                .build();

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new DuplicateEmailException("El correo ya registrado"));

        mockMvc.perform(post("/api/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }

    @Test
    void registerUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Juan Rodriguez")
                .email("invalid-email")
                .password("hunter2")
                .phones(Collections.emptyList())
                .build();

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new ValidationException("El formato del correo es inválido"));

        
        mockMvc.perform(post("/api/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("El formato del correo es inválido"));
    }

    @Test
    void registerUser_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("")
                .email("")
                .password("")
                .build();

        mockMvc.perform(post("/api/users/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").exists());
    }
}
